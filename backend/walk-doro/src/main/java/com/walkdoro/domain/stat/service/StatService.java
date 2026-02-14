package com.walkdoro.domain.stat.service;

import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.stat.dto.DailyStatResponse;
import com.walkdoro.domain.stat.dto.StatListResponse;
import com.walkdoro.domain.stat.dto.StepSyncRequest;
import com.walkdoro.domain.stat.dto.StepSyncResponse;
import com.walkdoro.domain.stat.repository.StatRepository;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StatService {
        private final UserRepository userRepository;
        private final StatRepository statRepository;

        @Transactional
        public StepSyncResponse syncSteps(Long userId, StepSyncRequest stepSyncRequest) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다."));

                Integer sentSteps = stepSyncRequest.steps();
                int upsertResult = statRepository.upsertSteps(userId, stepSyncRequest.date(), sentSteps);

                StepSyncResponse.Status status = switch (upsertResult) {
                        case 1 -> StepSyncResponse.Status.CREATED;
                        case 2 -> StepSyncResponse.Status.UPDATED;
                        case 0 -> StepSyncResponse.Status.IGNORED;
                        default -> throw new IllegalStateException("예상치 못한 업서트 결과입니다: " + upsertResult);
                };

                DailyStat dailyStat = statRepository.findByUserAndDate(user, stepSyncRequest.date())
                                .orElseThrow(() -> new IllegalStateException("업서트 후 데이터 조회에 실패했습니다."));

                return new StepSyncResponse(status, sentSteps, dailyStat.getStepCount(), dailyStat.getRewardBitMask());
        }

        @Transactional(readOnly = true)
        public StatListResponse getStats(Long userId, LocalDate startDate, LocalDate endDate) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자를 찾을 수 없습니다."));

                List<DailyStat> stats = statRepository.findAllByUserAndDateBetween(user, startDate, endDate);

                List<DailyStatResponse> dailyStatResponses = stats.stream()
                                .map(DailyStatResponse::from)
                                .toList();

                return StatListResponse.of(dailyStatResponses);
        }
}
