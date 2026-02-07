package com.walkdoro.global.auth.jwt;

import com.walkdoro.global.auth.jwt.JwtTokenParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenParserTest {

    private JwtTokenParser jwtTokenParser;
    private final String secretKeyPlain = "testSecretKeyMustBeLongEnoughForHmacSha256bitEncryption";
    private final long expirationTime = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        // ServletConfig is currently unused in the parser logic we are testing, so
        // passing null.
        jwtTokenParser = new JwtTokenParser(secretKeyPlain);
    }

    @Test
    @DisplayName("validateToken 은 유효한 토큰에 대해 true를 반환해야 한다")
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        // given
        String token = Jwts.builder()
                .subject("1")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(Keys.hmacShaKeyFor(secretKeyPlain.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // when
        boolean isValid = jwtTokenParser.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken 은 만료된 토큰에 대해 false를 반환해야 한다")
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        // given
        String token = Jwts.builder()
                .subject("1")
                .issuedAt(new Date(System.currentTimeMillis() - expirationTime * 2))
                .expiration(new Date(System.currentTimeMillis() - expirationTime))
                .signWith(Keys.hmacShaKeyFor(secretKeyPlain.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // when
        boolean isValid = jwtTokenParser.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken 은 잘못된 서명을 가진 토큰에 대해 false를 반환해야 한다")
    void validateToken_ShouldReturnFalse_WhenTokenHasInvalidSignature() {
        // given
        String invalidSecretKey = "invalidSecretKeyMustBeLongEnoughForHmacSha256bitEncryption";
        String token = Jwts.builder()
                .subject("1")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(Keys.hmacShaKeyFor(invalidSecretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // when
        boolean isValid = jwtTokenParser.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("extractClaims 는 유효한 토큰에서 Claims를 추출해야 한다")
    void extractClaims_ShouldReturnClaims_WhenTokenIsValid() {
        // given
        String sub = "1";
        String token = Jwts.builder()
                .subject(sub)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(Keys.hmacShaKeyFor(secretKeyPlain.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // when
        Claims claims = jwtTokenParser.parseClaims(token);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(sub);
    }
}
