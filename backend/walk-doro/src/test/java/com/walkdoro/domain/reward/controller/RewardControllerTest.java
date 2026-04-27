package com.walkdoro.domain.reward.controller;

import com.walkdoro.domain.reward.dto.RewardClaimResponse;
import com.walkdoro.domain.reward.service.RewardService;
import com.walkdoro.global.auth.annotation.LoginUser;
import com.walkdoro.global.error.GlobalExceptionHandler;
import com.walkdoro.global.idempotency.IdempotencyRecord;
import com.walkdoro.global.idempotency.IdempotencyService;
import com.walkdoro.global.idempotency.IdempotencyStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RewardControllerTest {

    private MockMvc mockMvc;
    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        rewardService = Mockito.mock(RewardService.class);
        IdempotencyService idempotencyService = new IdempotencyService(
                new InMemoryIdempotencyStore(),
                new ObjectMapper(),
                Clock.fixed(Instant.parse("2026-04-26T10:00:00Z"), ZoneOffset.UTC));

        HandlerMethodArgumentResolver loginUserResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(LoginUser.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return 1L;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new RewardController(rewardService, idempotencyService))
                .setCustomArgumentResolvers(loginUserResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("같은 Idempotency-Key와 같은 보상 수령 요청은 저장된 응답을 반환하고 서비스를 한 번만 호출")
    void claimReward_ReturnsStoredResponse_WhenSameIdempotencyKeyIsRetried() throws Exception {
        given(rewardService.claimReward(eq(1L), any()))
                .willReturn(new RewardClaimResponse(1L, 10L))
                .willReturn(new RewardClaimResponse(1L, 11L));

        String requestBody = "{\"date\":\"2026-04-26\",\"goalSteps\":1000}";

        mockMvc.perform(post("/api/v1/rewards/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "same-key")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUserPoints").value(10));

        mockMvc.perform(post("/api/v1/rewards/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "same-key")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUserPoints").value(10));

        verify(rewardService, times(1)).claimReward(eq(1L), any());
    }

    private static class InMemoryIdempotencyStore implements IdempotencyStore {

        private final Map<String, IdempotencyRecord> records = new HashMap<>();

        @Override
        public Optional<IdempotencyRecord> findByKey(String key) {
            return Optional.ofNullable(records.get(key));
        }

        @Override
        public boolean saveProcessingIfAbsent(String key, IdempotencyRecord record, Duration ttl) {
            if (records.containsKey(key)) {
                return false;
            }
            records.put(key, record);
            return true;
        }

        @Override
        public void saveCompleted(String key, IdempotencyRecord record, Duration ttl) {
            records.put(key, record);
        }

        @Override
        public void delete(String key) {
            records.remove(key);
        }
    }
}
