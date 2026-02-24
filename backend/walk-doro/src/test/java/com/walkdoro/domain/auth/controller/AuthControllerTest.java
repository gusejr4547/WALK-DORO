package com.walkdoro.domain.auth.controller;

import com.walkdoro.domain.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.Cookie;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new com.walkdoro.global.error.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("유효한 리프레시 토큰이 쿠키에 있으면 Access Token을 재발급한다")
    void reissue_ShouldReturnAccessToken_WhenCookieIsValid() throws Exception {
        // given
        String refreshToken = "valid_refresh";
        String newAccessToken = "new_access_token";

        given(authService.reissueAccessToken(refreshToken)).willReturn(newAccessToken);

        // when & then
        mockMvc.perform(post("/api/v1/auth/reissue")
                .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken));
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 재발급 요청 시 401 Unauthorized와 ErrorResponse를 반환한다")
    void reissue_ShouldReturnErrorResponse_WhenTokenIsInvalid() throws Exception {
        // given
        String invalidToken = "invalid_refresh";

        given(authService.reissueAccessToken(invalidToken))
                .willThrow(new com.walkdoro.global.error.exception.BusinessException(
                        com.walkdoro.global.error.ErrorCode.INVALID_REFRESH_TOKEN));

        // when & then
        mockMvc.perform(post("/api/v1/auth/reissue")
                .cookie(new Cookie("refresh_token", invalidToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A002"));
    }

    @Test
    @DisplayName("로그아웃 시 리프레시 토큰을 삭제하고 쿠키를 만료시킨다")
    void logout_ShouldDeleteTokenAndClearCookie() throws Exception {
        // given
        String refreshToken = "refresh_token";

        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("LOGOUT_SUCCESS"))
                .andExpect(cookie().maxAge("refresh_token", 0));

        // verify service logout called
        org.mockito.Mockito.verify(authService).logout(refreshToken, null);
    }
}
