package com.walkdoro.global.auth.jwt;

import com.walkdoro.global.auth.jwt.JwtTokenParser;
import com.walkdoro.global.auth.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.Collections;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtTokenParser jwtTokenParser;
    private UserDetailsService userDetailsService;
    private final String secretKeyPlain = "testSecretKeyMustBeLongEnoughForHmacSha256bitEncryption";
    private final long expirationTime = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenParser = new JwtTokenParser(secretKeyPlain);
        userDetailsService = mock(UserDetailsService.class);
        jwtTokenProvider = new JwtTokenProvider(secretKeyPlain, expirationTime, expirationTime * 2, jwtTokenParser,
                userDetailsService);
    }

    @Test
    @DisplayName("createAccessToken 생성 테스트 - 토큰이 정상적으로 생성되어야 한다")
    void createAccessToken_Validate() {
        // given
        Long userId = 1L;
        String role = "ROLE_USER";

        // when
        String token = jwtTokenProvider.createAccessToken(userId, role);

        // then
        assertThat(token).isNotNull();

        // 검증: 토큰 파싱해서 내용 확인
        SecretKey key = Keys.hmacShaKeyFor(secretKeyPlain.getBytes(StandardCharsets.UTF_8));
        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        assertThat(subject).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("getAuthentication 테스트 - 유효한 토큰으로 인증 객체를 반환해야 한다")
    void getAuthentication_ShouldReturnAuthentication() {
        // given
        Long userId = 1L;
        String role = "ROLE_USER";
        String token = jwtTokenProvider.createAccessToken(userId, role);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                userId.toString(), "", Collections.emptyList());

        when(userDetailsService.loadUserByUsername(userId.toString())).thenReturn(userDetails);

        // when
        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
    }

    @Test
    @DisplayName("createRefreshToken 생성 테스트 - 리프레시 토큰이 정상적으로 생성되어야 한다")
    void createRefreshToken_Validate() {
        // given
        Long userId = 1L;
        String role = "ROLE_USER";

        // when
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, role);

        // then
        assertThat(refreshToken).isNotNull();

        // 검증: 만료 시간이 액세스 토큰보다 길어야 함 (단순 파싱 확인)
        SecretKey key = Keys.hmacShaKeyFor(secretKeyPlain.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        // expiration check logic could be refined but existence is key here
    }

    @Test
    @DisplayName("validateToken 은 리프레시 토큰에 대해서도 true를 반환해야 한다")
    void validateToken_ShouldReturnTrue_ForRefreshToken() {
        // given
        Long userId = 1L;
        String role = "ROLE_USER";
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, role);

        // when
        boolean isValid = jwtTokenProvider.validateToken(refreshToken);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Refresh Token은 Access Token보다 만료 시간이 길어야 한다")
    void refreshToken_ShouldHaveLongerExpiration() {
        // given
        Long userId = 1L;
        String role = "ROLE_USER";

        // when
        String accessToken = jwtTokenProvider.createAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, role);

        // then
        SecretKey key = Keys.hmacShaKeyFor(secretKeyPlain.getBytes(StandardCharsets.UTF_8));

        Date accessExpiration = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getExpiration();

        Date refreshExpiration = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getExpiration();

        assertThat(refreshExpiration).isAfter(accessExpiration);
    }

    @Test
    @DisplayName("isAccessToken은 ACCESS 타입 토큰에 대해 true를 반환해야 한다")
    void isAccessToken_ShouldReturnTrue_ForAccessToken() {
        // given
        String token = jwtTokenProvider.createAccessToken(1L, "ROLE_USER");

        // when
        boolean result = jwtTokenProvider.isAccessToken(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isRefreshToken은 REFRESH 타입 토큰에 대해 true를 반환해야 한다")
    void isRefreshToken_ShouldReturnTrue_ForRefreshToken() {
        // given
        String token = jwtTokenProvider.createRefreshToken(1L, "ROLE_USER");

        // when
        boolean result = jwtTokenProvider.isRefreshToken(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isAccessToken은 REFRESH 타입 토큰에 대해 false를 반환해야 한다")
    void isAccessToken_ShouldReturnFalse_ForRefreshToken() {
        // given
        String token = jwtTokenProvider.createRefreshToken(1L, "ROLE_USER");

        // when
        boolean result = jwtTokenProvider.isAccessToken(token);

        // then
        assertThat(result).isFalse();
    }
}
