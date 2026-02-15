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
import com.walkdoro.global.error.exception.BusinessException;
import com.walkdoro.global.error.ErrorCode;

@RequiredArgsConstructor
@Service
public class RewardService {
    private final UserRepository userRepository;
    private final StatRepository statRepository;

    @Transactional
    public RewardClaimResponse claimReward(Long userId, RewardClaimRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        DailyStat dailyStat = statRepository.findByUserAndDateWithLock(user, request.date())
                .orElseThrow(() -> new BusinessException(ErrorCode.STAT_NOT_FOUND));

        int goalSteps = request.goalSteps();
        validateGoalSteps(goalSteps);

        if (dailyStat.getStepCount() < goalSteps) {
            throw new BusinessException(ErrorCode.REWARD_GOAL_NOT_REACHED);
        }

        if (dailyStat.isRewardClaimed(goalSteps)) {
            throw new BusinessException(ErrorCode.REWARD_ALREADY_CLAIMED);
        }

        long pointsToAdd = 1L;
        dailyStat.claimReward(goalSteps);
        userRepository.updatePoint(userId, pointsToAdd);

        return new RewardClaimResponse(pointsToAdd, user.getPoint() + pointsToAdd);
    }

    private void validateGoalSteps(int goalSteps) {
        if (goalSteps <= 10000) {
            if (goalSteps <= 0 || goalSteps % 1000 != 0) {
                throw new BusinessException(ErrorCode.INVALID_REWARD_STEP_UNIT);
            }
            return;
        }

        if (goalSteps % 2000 != 0) {
            throw new BusinessException(ErrorCode.INVALID_REWARD_STEP_UNIT);
        }
    }
}
