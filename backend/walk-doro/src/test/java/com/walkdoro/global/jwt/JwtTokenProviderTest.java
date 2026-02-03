package com.walkdoro.global.jwt;

import com.walkdoro.global.auth.jwt.JwtTokenParser;
import com.walkdoro.global.auth.jwt.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

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
        jwtTokenProvider = new JwtTokenProvider(secretKeyPlain, expirationTime, jwtTokenParser, userDetailsService);
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
}
