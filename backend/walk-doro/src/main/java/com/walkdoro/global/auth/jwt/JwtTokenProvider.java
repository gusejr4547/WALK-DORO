package com.walkdoro.global.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private final JwtTokenParser jwtTokenParser;
    private final SecretKey secretKey;
    private final long expirationTime;
    private final long refreshTokenExpirationTime;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String key,
            @Value("${jwt.expiration}") long expirationTime,
            @Value("${jwt.refresh-expiration}") long refreshTokenExpirationTime,
            JwtTokenParser jwtTokenParser,
            UserDetailsService userDetailsService) {
        this.secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
        this.jwtTokenParser = jwtTokenParser;
        this.userDetailsService = userDetailsService;
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

    /*
     * 이 메서드는 “토큰 문자열”을 받아서 Spring Security의 Authentication을 만들어 반환하는 역할이다.
     * 흐름은 보통:
     * token → claims 파싱
     * claims에서 sub(권장: userId) 추출
     * UserDetailsService로 사용자 로딩
     * UsernamePasswordAuthenticationToken 생성 후 반환
     * 즉, 기존에 JwtFilter가 직접 하던 “DB 조회 + Authentication 생성”을 Provider로 옮겨서 필터를 얇게만든다.
     */
    public Authentication getAuthentication(String token) {
        Claims claims = jwtTokenParser.parseClaims(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
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
