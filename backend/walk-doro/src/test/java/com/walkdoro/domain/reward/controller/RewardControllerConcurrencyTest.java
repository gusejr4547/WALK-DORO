package com.walkdoro.domain.reward.controller;

import com.walkdoro.domain.reward.service.RewardService;
import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.stat.repository.StatRepository;
import com.walkdoro.domain.user.Role;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import com.walkdoro.global.auth.annotation.loginuser.LoginUserArgumentResolver;
import com.walkdoro.global.auth.annotation.loginuser.UserAdapter;
import com.walkdoro.global.error.GlobalExceptionHandler;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
class RewardControllerConcurrencyTest {

    private MockMvc mockMvc;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatRepository statRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RewardController(rewardService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        statRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("동일 유저의 보상수령 동시 HTTP 요청은 1건만 성공한다")
    void claimReward_ShouldSucceedOnce_WhenConcurrentHttpRequestsAreSent() throws Exception {
        User savedUser = userRepository.save(User.builder()
                .name("http-tester")
                .email("http-tester@example.com")
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

        UserAdapter userAdapter = new UserAdapter(savedUser.getId().toString(), Role.USER.getKey());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userAdapter,
                null,
                userAdapter.getAuthorities());
        String requestBody = "{\"date\":\"" + today + "\",\"goalSteps\":" + goalSteps + "}";

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
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    MvcResult result = mockMvc.perform(post("/api/v1/rewards/claims")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                            .andReturn();

                    int status = result.getResponse().getStatus();
                    if (status == 200) {
                        successCount.incrementAndGet();
                        return;
                    }

                    String responseBody = result.getResponse().getContentAsString();
                    if (status == 400 && responseBody.contains("\"code\":\"R001\"")) {
                        alreadyClaimedCount.incrementAndGet();
                        return;
                    }

                    unexpectedFailureCount.incrementAndGet();
                } catch (Exception e) {
                    unexpectedFailureCount.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
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
