package com.walkdoro.domain.reward.service;

import com.walkdoro.domain.reward.dto.RewardClaimRequest;
import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.stat.repository.StatRepository;
import com.walkdoro.domain.user.Role;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import com.walkdoro.global.error.ErrorCode;
import com.walkdoro.global.error.exception.BusinessException;
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
class RewardServiceConcurrencyTest {

    @Autowired
    private RewardService rewardService;

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
    @DisplayName("한 유저가 동일 보상 요청을 동시에 여러 번 보내도 포인트는 1회만 증가한다")
    void claimReward_ShouldUpdatePointOnlyOnce_WhenDuplicateRequestsAreConcurrent() throws Exception {
        User savedUser = userRepository.save(User.builder()
                .name("tester")
                .email("tester@example.com")
                .role(Role.USER)
                .build());

        LocalDate today = LocalDate.now();
        int goalSteps = 1000;
        int requestCount = 20;

        statRepository.save(DailyStat.builder()
                .user(savedUser)
                .date(today)
                .stepCount(goalSteps)
                .build());

        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(requestCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger alreadyClaimedCount = new AtomicInteger();
        AtomicInteger unexpectedFailureCount = new AtomicInteger();

        for (int i = 0; i < requestCount; i++) {
            executorService.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    rewardService.claimReward(savedUser.getId(), new RewardClaimRequest(today, goalSteps));
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    if (e.getErrorCode() == ErrorCode.REWARD_ALREADY_CLAIMED) {
                        alreadyClaimedCount.incrementAndGet();
                    } else {
                        unexpectedFailureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    unexpectedFailureCount.incrementAndGet();
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
        DailyStat updatedStat = statRepository.findByUserAndDate(updatedUser, today).orElseThrow();

        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(alreadyClaimedCount.get()).isEqualTo(requestCount - 1);
        assertThat(unexpectedFailureCount.get()).isZero();
        assertThat(updatedUser.getPoint()).isEqualTo(1L);
        assertThat(updatedStat.getRewardedPoints()).isEqualTo(1L);
    }
}
