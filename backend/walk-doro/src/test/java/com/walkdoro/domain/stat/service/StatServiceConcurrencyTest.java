package com.walkdoro.domain.stat.service;

import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.stat.dto.StepSyncRequest;
import com.walkdoro.domain.stat.repository.StatRepository;
import com.walkdoro.domain.user.Role;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StatServiceConcurrencyTest {

    @Autowired
    private StatService statService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatRepository statRepository;

    @AfterEach
    void tearDown() {
        statRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 날짜로 동시 동기화 요청이 와도 최종 저장 걸음수는 최대값이다")
    void syncSteps_ShouldConvergeToMaxStepCount_WhenConcurrentRequestsAreSent() throws Exception {
        User savedUser = userRepository.save(User.builder()
                .name("sync-tester")
                .email("sync-tester@example.com")
                .role(Role.USER)
                .build());

        LocalDate today = LocalDate.now();
        int[] sentSteps = { 400, 1200, 800, 2000, 1500, 2000, 300, 1000, 1800, 700 };
        int requestCount = sentSteps.length;

        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(requestCount);

        AtomicInteger failureCount = new AtomicInteger();

        for (int steps : sentSteps) {
            executorService.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    statService.syncSteps(savedUser.getId(), new StepSyncRequest(today, steps));
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        boolean finished = done.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        DailyStat dailyStat = statRepository.findByUserAndDate(updatedUser, today).orElseThrow();

        assertThat(finished).isTrue();
        assertThat(failureCount.get()).isZero();
        assertThat(dailyStat.getStepCount()).isEqualTo(2000);
        assertThat(dailyStat.getRewardBitMask()).isZero();
    }
}
