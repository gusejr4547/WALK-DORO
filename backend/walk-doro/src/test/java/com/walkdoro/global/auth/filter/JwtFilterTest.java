package com.walkdoro.global.auth.filter;

import com.walkdoro.domain.auth.repository.RefreshTokenRepository;
import com.walkdoro.global.auth.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class JwtFilterTest {

    private JwtFilter jwtFilter;
    private JwtTokenProvider jwtTokenProvider;
    private RefreshTokenRepository refreshTokenRepository;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        jwtFilter = new JwtFilter(jwtTokenProvider, refreshTokenRepository);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("resolveToken - 헤더에서 Bearer 토큰을 정확히 추출해야 한다")
    void resolveToken_ShouldExtractToken_WhenHeaderIsValid() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer targetToken");

        // when
        String token = jwtFilter.resolveToken(request);

        // then
        assertThat(token).isEqualTo("targetToken");
    }

    @Test
    @DisplayName("resolveToken - 헤더가 없거나 잘못된 형식이면 null을 반환해야 한다")
    void resolveToken_ShouldReturnNull_WhenHeaderIsInvalid() {
        // given
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.addHeader("Authorization", "Basic invalidToken");

        // when
        String token1 = jwtFilter.resolveToken(request1);
        String token2 = jwtFilter.resolveToken(request2);

        // then
        assertThat(token1).isNull();
        assertThat(token2).isNull();
    }

    @Test
    @DisplayName("doFilterInternal - 유효한 토큰이 있을 때 SecurityContext에 인증을 설정해야 한다")
    void doFilterInternal_ShouldSetAuthentication_WhenTokenIsValid() throws ServletException, IOException {
        // given
        String validToken = "validToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication authentication = mock(Authentication.class);
        given(jwtTokenProvider.validateToken(validToken)).willReturn(true);
        given(jwtTokenProvider.isAccessToken(validToken)).willReturn(true);
        given(jwtTokenProvider.getAuthentication(validToken)).willReturn(authentication);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).isAccessToken(validToken);
        verify(jwtTokenProvider).getAuthentication(validToken);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - 유효하지 않은 토큰일 때 SecurityContext에 인증을 설정하지 않아야 한다")
    void doFilterInternal_ShouldNotSetAuthentication_WhenTokenIsInvalid() throws ServletException, IOException {
        // given
        String invalidToken = "invalidToken";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + invalidToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(jwtTokenProvider).validateToken(invalidToken);
        verify(jwtTokenProvider, never()).getAuthentication(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
