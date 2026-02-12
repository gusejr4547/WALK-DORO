package com.walkdoro.domain.stat.service;

import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.stat.dto.StepSyncRequest;
import com.walkdoro.domain.stat.dto.StepSyncResponse;
import com.walkdoro.domain.stat.repository.StatRepository;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatServiceTest {

    @InjectMocks
    private StatService statService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StatRepository statRepository;

    @Test
    @DisplayName("기존 기록이 있으면 걸음 수를 업데이트한다")
    void syncSteps_ShouldUpdateStepCount_WhenStatExists() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 1, 1);
        int initialSteps = 1000;
        int newSteps = 2000;

        User user = User.builder().build();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", userId);

        DailyStat existingStat = DailyStat.builder()
                .user(user)
                .date(date)
                .stepCount(initialSteps)
                .build();

        StepSyncRequest request = new StepSyncRequest(date, newSteps);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(statRepository.findByUserAndDate(user, date)).willReturn(Optional.of(existingStat));

        // when
        StepSyncResponse response = statService.syncSteps(userId, request);

        // then
        assertThat(existingStat.getStepCount()).isEqualTo(newSteps);
        assertThat(response.storedStepCount()).isEqualTo(newSteps);
        assertThat(response.status()).isEqualTo(StepSyncResponse.Status.UPDATED);
    }

    @Test
    @DisplayName("기존 기록이 없으면 새로운 기록을 생성한다")
    void syncSteps_ShouldCreateNewStat_WhenStatDoesNotExist() {
        // given
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 1, 1);
        int steps = 1000;

        User user = User.builder().build();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", userId);

        StepSyncRequest request = new StepSyncRequest(date, steps);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(statRepository.findByUserAndDate(user, date)).willReturn(Optional.empty());

        // when
        StepSyncResponse response = statService.syncSteps(userId, request);

        // then
        verify(statRepository).save(any(DailyStat.class));
        assertThat(response.storedStepCount()).isEqualTo(steps);
        assertThat(response.status()).isEqualTo(StepSyncResponse.Status.CREATED);
    }

    @Test
    @DisplayName("기간으로 통계를 조회한다")
    void getStats_ShouldReturnStats_WhenExists() {
        // given
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 7);

        User user = User.builder().build();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", userId);

        DailyStat stat1 = DailyStat.builder().user(user).date(startDate).stepCount(1000).build();
        DailyStat stat2 = DailyStat.builder().user(user).date(endDate).stepCount(2000).build();
        List<DailyStat> stats = List.of(stat1, stat2);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(statRepository.findAllByUserAndDateBetween(user, startDate, endDate)).willReturn(stats);

        // when
        com.walkdoro.domain.stat.dto.StatListResponse response = statService.getStats(userId, startDate, endDate);

        // then
        assertThat(response.dailyStats()).hasSize(2);
        assertThat(response.totalSteps()).isEqualTo(3000);
    }
}
