package com.walkdoro.domain.auth.service;

import com.walkdoro.domain.auth.repository.RefreshTokenRepository;
import com.walkdoro.global.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public String reissueAccessToken(String refreshToken) {
        // 1. 토큰 형식 검증
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        // 2. Redis에서 토큰 존재 여부 확인 (만료되거나 탈취된 토큰인지 확인)
        Long userId = refreshTokenRepository.findUserIdByRefreshToken(refreshToken)
                .orElseThrow(
                        () -> new IllegalArgumentException("Refresh Token not found in server (Expired or Invalid)"));

        // 3. 토큰에서 유저 정보 추출 (또는 Redis에 저장된 userId 사용)
        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        return jwtTokenProvider.createAccessToken(userId, role);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.delete(refreshToken);
        }
    }
}
