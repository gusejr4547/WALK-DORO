package com.walkdoro.domain.reward.service;

import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.reward.dto.RewardClaimRequest;
import com.walkdoro.domain.reward.dto.RewardClaimResponse;
import com.walkdoro.domain.stat.repository.StatRepository;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RewardService {
    private final UserRepository userRepository;
    private final StatRepository statRepository;

    @Transactional
    public RewardClaimResponse claimReward(Long userId, RewardClaimRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("id에 해당하는 유저가 없습니다."));

        DailyStat dailyStat = statRepository.findByUserAndDateWithLock(user, request.date())
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜의 기록이 없습니다."));

        int goalSteps = request.goalSteps();
        validateGoalSteps(goalSteps);

        if (dailyStat.getStepCount() < goalSteps) {
            throw new IllegalArgumentException("아직 목표 걸음 수에 도달하지 못했습니다.");
        }

        if (dailyStat.isRewardClaimed(goalSteps)) {
            throw new IllegalArgumentException("이미 해당 구간의 보상을 수령했습니다.");
        }

        long pointsToAdd = 1L;
        dailyStat.claimReward(goalSteps);
        userRepository.updatePoint(userId, pointsToAdd);

        return new RewardClaimResponse(pointsToAdd, user.getPoint() + pointsToAdd);
    }

    private void validateGoalSteps(int goalSteps) {
        if (goalSteps <= 10000) {
            if (goalSteps <= 0 || goalSteps % 1000 != 0) {
                throw new IllegalArgumentException("10000보 이하에서는 1000보 단위로만 보상을 수령할 수 있습니다.");
            }
            return;
        }

        if (goalSteps % 2000 != 0) {
            throw new IllegalArgumentException("10000보 초과 구간에서는 2000보 단위로만 보상을 수령할 수 있습니다.");
        }
    }
}
