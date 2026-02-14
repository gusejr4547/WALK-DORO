package com.walkdoro.domain.stat.service;

import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.stat.dto.StepSyncRequest;
import com.walkdoro.domain.stat.dto.StepSyncResponse;
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
    @DisplayName("Returns CREATED when row is inserted")
    void syncSteps_ShouldReturnCreated_WhenInserted() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 1, 1);
        int steps = 1000;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        DailyStat dailyStat = DailyStat.builder()
                .user(user)
                .date(date)
                .stepCount(steps)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(statRepository.upsertSteps(userId, date, steps)).willReturn(1);
        given(statRepository.findByUserAndDate(user, date)).willReturn(Optional.of(dailyStat));

        StepSyncResponse response = statService.syncSteps(userId, new StepSyncRequest(date, steps));

        verify(statRepository).upsertSteps(userId, date, steps);
        assertThat(response.status()).isEqualTo(StepSyncResponse.Status.CREATED);
        assertThat(response.storedStepCount()).isEqualTo(steps);
    }

    @Test
    @DisplayName("Returns UPDATED when step count increases")
    void syncSteps_ShouldReturnUpdated_WhenRaised() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 1, 1);
        int sentSteps = 2000;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        DailyStat dailyStat = DailyStat.builder()
                .user(user)
                .date(date)
                .stepCount(sentSteps)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(statRepository.upsertSteps(userId, date, sentSteps)).willReturn(2);
        given(statRepository.findByUserAndDate(user, date)).willReturn(Optional.of(dailyStat));

        StepSyncResponse response = statService.syncSteps(userId, new StepSyncRequest(date, sentSteps));

        assertThat(response.status()).isEqualTo(StepSyncResponse.Status.UPDATED);
        assertThat(response.storedStepCount()).isEqualTo(sentSteps);
    }

    @Test
    @DisplayName("Returns IGNORED when value does not increase")
    void syncSteps_ShouldReturnIgnored_WhenNoChange() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 1, 1);
        int sentSteps = 1000;
        int storedSteps = 2000;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        DailyStat dailyStat = DailyStat.builder()
                .user(user)
                .date(date)
                .stepCount(storedSteps)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(statRepository.upsertSteps(userId, date, sentSteps)).willReturn(0);
        given(statRepository.findByUserAndDate(user, date)).willReturn(Optional.of(dailyStat));

        StepSyncResponse response = statService.syncSteps(userId, new StepSyncRequest(date, sentSteps));

        assertThat(response.status()).isEqualTo(StepSyncResponse.Status.IGNORED);
        assertThat(response.storedStepCount()).isEqualTo(storedSteps);
    }
}
