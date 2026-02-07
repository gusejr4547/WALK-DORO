package com.walkdoro.global.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component
@Slf4j
public class JwtTokenProvider {

    private final JwtTokenParser jwtTokenParser;
    private final SecretKey secretKey;
    private final long expirationTime;
    private final long refreshTokenExpirationTime;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String key,
            @Value("${jwt.expiration}") long expirationTime,
            @Value("${jwt.refresh-expiration}") long refreshTokenExpirationTime,
            JwtTokenParser jwtTokenParser) {
        this.secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
        this.jwtTokenParser = jwtTokenParser;
    }

    // 사용자 정보로 엑세스 토큰 만들기
    public String createAccessToken(Long userId, String role) {
        Claims claims = Jwts.claims()
                .subject(userId.toString())
                .add("role", role)
                .add("type", "ACCESS")
                .build();
        Date issuedAt = new Date();
        Date expiredAt = new Date(issuedAt.getTime() + expirationTime);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiredAt)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long userId, String role) {
        Claims claims = Jwts.claims()
                .subject(userId.toString())
                .add("role", role)
                .add("type", "REFRESH")
                .build();
        Date issuedAt = new Date();
        // Refresh Token은 설정된 만료 시간 사용
        Date expiredAt = new Date(issuedAt.getTime() + refreshTokenExpirationTime);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiredAt)
                .signWith(secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = jwtTokenParser.parseClaims(token);

        // DB 조회 없이 토큰에서 바로 정보 추출
        String userId = claims.getSubject();
        String role = claims.get("role", String.class);

        if (role == null) {
            role = "ROLE_USER"; // Default fallback
        }

        Collection<? extends GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(role));

        // UserDetails 호환성을 위해 Spring Security User 객체 생성 (비밀번호는 빈 값)
        org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
                userId, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        return jwtTokenParser.validateToken(token);
    }

    public boolean isAccessToken(String token) {
        String type = (String) jwtTokenParser.parseClaims(token).get("type");
        return "ACCESS".equals(type);
    }

    public boolean isRefreshToken(String token) {
        String type = (String) jwtTokenParser.parseClaims(token).get("type");
        return "REFRESH".equals(type);
    }

    public long getExpiration(String token) {
        Date expiration = jwtTokenParser.parseClaims(token).getExpiration();
        long now = new Date().getTime();
        return expiration.getTime() - now;
    }
}
