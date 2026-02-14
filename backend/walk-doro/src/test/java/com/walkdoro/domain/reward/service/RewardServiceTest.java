package com.walkdoro.domain.reward.service;

import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.reward.dto.RewardClaimRequest;
import com.walkdoro.domain.reward.dto.RewardClaimResponse;
import com.walkdoro.domain.stat.repository.StatRepository;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @InjectMocks
    private RewardService rewardService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StatRepository statRepository;

    @Test
    @DisplayName("10000보 이하 구간 보상을 지급한다")
    void claimReward_ShouldGrantPoints_WhenGoalMet_Under10k() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 1, 1);
        int goalSteps = 1000;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        DailyStat dailyStat = DailyStat.builder()
                .user(user)
                .date(date)
                .stepCount(goalSteps)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(statRepository.findByUserAndDateWithLock(user, date)).willReturn(Optional.of(dailyStat));

        RewardClaimResponse response = rewardService.claimReward(userId, new RewardClaimRequest(date, goalSteps));

        assertThat(response.claimedPoints()).isEqualTo(1L);
        assertThat(response.totalUserPoints()).isEqualTo(1L);
        verify(userRepository).updatePoint(userId, 1L);
        assertThat(dailyStat.getRewardedPoints()).isEqualTo(1L);
    }

    @Test
    @DisplayName("10000보 초과 구간 보상을 누적 지급한다")
    void claimReward_ShouldGrantPoints_WhenGoalMet_Over10k() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 1, 1);
        int goalSteps = 12000;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        DailyStat dailyStat = DailyStat.builder()
                .user(user)
                .date(date)
                .stepCount(goalSteps)
                .build();
        for (int i = 1000; i <= 10000; i += 1000) {
            dailyStat.claimReward(i);
        }
        user.addPoint(10L);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(statRepository.findByUserAndDateWithLock(user, date)).willReturn(Optional.of(dailyStat));

        RewardClaimResponse response = rewardService.claimReward(userId, new RewardClaimRequest(date, goalSteps));

        assertThat(response.claimedPoints()).isEqualTo(1L);
        assertThat(response.totalUserPoints()).isEqualTo(11L);
        verify(userRepository).updatePoint(userId, 1L);
        assertThat(dailyStat.getRewardedPoints()).isEqualTo(11L);
    }

    @Test
    @DisplayName("이미 수령한 보상을 다시 요청하면 예외가 발생한다")
    void claimReward_ShouldThrow_WhenAlreadyClaimed() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 1, 1);
        int goalSteps = 1000;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        DailyStat dailyStat = DailyStat.builder()
                .user(user)
                .date(date)
                .stepCount(goalSteps)
                .build();
        dailyStat.claimReward(goalSteps);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(statRepository.findByUserAndDateWithLock(user, date)).willReturn(Optional.of(dailyStat));

        assertThatThrownBy(() -> rewardService.claimReward(userId, new RewardClaimRequest(date, goalSteps)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 해당 구간의 보상을 수령했습니다.");
    }

    @Test
    @DisplayName("잘못된 구간 단위 요청이면 예외가 발생한다")
    void claimReward_ShouldThrow_WhenInvalidInterval() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 1, 1);
        int goalSteps = 1500;

        User user = User.builder().build();
        DailyStat dailyStat = DailyStat.builder().user(user).date(date).stepCount(2000).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(statRepository.findByUserAndDateWithLock(user, date)).willReturn(Optional.of(dailyStat));

        assertThatThrownBy(() -> rewardService.claimReward(userId, new RewardClaimRequest(date, goalSteps)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("목표 걸음 수를 달성하지 못하면 예외가 발생한다")
    void claimReward_ShouldThrow_WhenStepsInsufficient() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 1, 1);
        int goalSteps = 2000;

        User user = User.builder().build();
        DailyStat dailyStat = DailyStat.builder().user(user).date(date).stepCount(1000).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(statRepository.findByUserAndDateWithLock(user, date)).willReturn(Optional.of(dailyStat));

        assertThatThrownBy(() -> rewardService.claimReward(userId, new RewardClaimRequest(date, goalSteps)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
