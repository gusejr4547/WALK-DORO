package com.walkdoro.domain.stat.controller;

import com.walkdoro.domain.stat.dto.StepSyncResponse;
import com.walkdoro.domain.stat.service.StatService;
import com.walkdoro.domain.user.Role;
import com.walkdoro.global.auth.annotation.loginuser.LoginUserArgumentResolver;
import com.walkdoro.global.auth.annotation.loginuser.UserAdapter;
import com.walkdoro.global.error.GlobalExceptionHandler;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StatService statService;

    @InjectMocks
    private StatController statController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(statController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new LoginUserArgumentResolver())
                .setValidator(validator)
                .build();

        UserAdapter userAdapter = new UserAdapter("1", Role.USER.getKey());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userAdapter, null, userAdapter.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("syncSteps returns 201 when service status is CREATED")
    void syncSteps_ShouldReturnCreatedStatus_WhenServiceReturnsCreated() throws Exception {
        LocalDate date = LocalDate.of(2024, 1, 1);
        StepSyncResponse response = StepSyncResponse.created(1000, 1000, 0L);
        given(statService.syncSteps(eq(1L), any())).willReturn(response);

        mockMvc.perform(post("/api/v1/stats/steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"" + date + "\",\"steps\":1000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.storedStepCount").value(1000));
    }

    @Test
    @DisplayName("syncSteps returns 200 when service status is not CREATED")
    void syncSteps_ShouldReturnOkStatus_WhenServiceReturnsUpdatedOrIgnored() throws Exception {
        LocalDate date = LocalDate.of(2024, 1, 1);
        StepSyncResponse response = StepSyncResponse.updated(2000, 2000, 0L);
        given(statService.syncSteps(eq(1L), any())).willReturn(response);

        mockMvc.perform(post("/api/v1/stats/steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"" + date + "\",\"steps\":2000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UPDATED"))
                .andExpect(jsonPath("$.storedStepCount").value(2000));
    }

    @Test
    @DisplayName("syncSteps rejects future date request with 400")
    void syncSteps_ShouldRejectFutureDate() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        mockMvc.perform(post("/api/v1/stats/steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"" + futureDate + "\",\"steps\":1000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));

        verifyNoInteractions(statService);
    }

    @Test
    @DisplayName("syncSteps rejects negative steps request with 400")
    void syncSteps_ShouldRejectNegativeSteps() throws Exception {
        LocalDate date = LocalDate.of(2024, 1, 1);

        mockMvc.perform(post("/api/v1/stats/steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"" + date + "\",\"steps\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));

        verifyNoInteractions(statService);
    }
}
