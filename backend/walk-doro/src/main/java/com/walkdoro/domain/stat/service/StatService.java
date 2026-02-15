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

import com.walkdoro.global.error.exception.BusinessException;
import com.walkdoro.global.error.ErrorCode;

@RequiredArgsConstructor
@Service
public class StatService {
        private final UserRepository userRepository;
        private final StatRepository statRepository;

        @Transactional
        public StepSyncResponse syncSteps(Long userId, StepSyncRequest stepSyncRequest) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                Integer sentSteps = stepSyncRequest.steps();
                int upsertResult = statRepository.upsertSteps(userId, stepSyncRequest.date(), sentSteps);

                StepSyncResponse.Status status = switch (upsertResult) {
                        case 1 -> StepSyncResponse.Status.CREATED;
                        case 2 -> StepSyncResponse.Status.UPDATED;
                        case 0 -> StepSyncResponse.Status.IGNORED;
                        default -> throw new BusinessException(ErrorCode.STAT_UPSERT_FAILED);
                };

                DailyStat dailyStat = statRepository.findByUserAndDate(user, stepSyncRequest.date())
                                .orElseThrow(() -> new BusinessException(ErrorCode.STAT_LOOKUP_FAILED));

                return new StepSyncResponse(status, sentSteps, dailyStat.getStepCount(), dailyStat.getRewardBitMask());
        }

        @Transactional(readOnly = true)
        public StatListResponse getStats(Long userId, LocalDate startDate, LocalDate endDate) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                List<DailyStat> stats = statRepository.findAllByUserAndDateBetween(user, startDate, endDate);

                List<DailyStatResponse> dailyStatResponses = stats.stream()
                                .map(DailyStatResponse::from)
                                .toList();

                return StatListResponse.of(dailyStatResponses);
        }
}
