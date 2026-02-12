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
                                .orElseThrow(() -> new IllegalArgumentException("id에 해당하는 유저가 없습니다."));

                Integer sentSteps = stepSyncRequest.steps();

                return statRepository.findByUserAndDate(user, stepSyncRequest.date())
                                .map(dailyStat -> {
                                        if (dailyStat.getStepCount() < sentSteps) {
                                                dailyStat.updateStepCount(sentSteps);
                                                return StepSyncResponse.updated(sentSteps, dailyStat.getStepCount(),
                                                                dailyStat.getRewardedPoints());
                                        }
                                        return StepSyncResponse.ignored(sentSteps, dailyStat.getStepCount(),
                                                        dailyStat.getRewardedPoints());
                                })
                                .orElseGet(() -> {
                                        DailyStat dailyStat = DailyStat.builder()
                                                        .user(user)
                                                        .date(stepSyncRequest.date())
                                                        .stepCount(sentSteps)
                                                        .build();
                                        statRepository.save(dailyStat);
                                        return StepSyncResponse.created(sentSteps, dailyStat.getStepCount(),
                                                        dailyStat.getRewardedPoints());
                                });
        }

        @Transactional(readOnly = true)
        public StatListResponse getStats(Long userId, LocalDate startDate, LocalDate endDate) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("id에 해당하는 유저가 없습니다."));

                List<DailyStat> stats = statRepository.findAllByUserAndDateBetween(user, startDate, endDate);

                List<DailyStatResponse> dailyStatResponses = stats.stream()
                                .map(DailyStatResponse::from)
                                .toList();

                return StatListResponse.of(dailyStatResponses);
        }
}
