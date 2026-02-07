package com.walkdoro.global.auth.handler;

import com.walkdoro.domain.auth.repository.RefreshTokenRepository;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import com.walkdoro.global.auth.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

        private final JwtTokenProvider jwtTokenProvider;
        private final UserRepository userRepository;
        private final RefreshTokenRepository refreshTokenRepository;

        @Value("${jwt.refresh-expiration}")
        private long refreshTokenExpiration; // Refresh Token Expiration

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException, ServletException {
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                Map<String, Object> attributes = oAuth2User.getAttributes();
                String email = (String) attributes.get("email"); // 구글은 email 필드 제공

                // DB에서 유저 조회 (ID를 얻기 위해)
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 유저가 없습니다."));

                // Refresh Token 생성
                String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getRoleKey());

                // Redis에 저장 (14일)
                // Redis에 저장
                long refreshExpiration = refreshTokenExpiration;
                refreshTokenRepository.save(refreshToken, user.getId(), refreshExpiration);

                // Refresh Token을 HttpOnly Cookie에 설정
                Cookie refreshCookie = new Cookie("refresh_token",
                                refreshToken);
                refreshCookie.setHttpOnly(true);
                refreshCookie.setSecure(false); // 로컬 개발 환경에서는 false, 배포 시 true (HTTPS 필요)
                refreshCookie.setPath("/");
                refreshCookie.setMaxAge((int) (refreshExpiration / 1000));

                response.addCookie(refreshCookie);

                log.info("로그인 성공. Refresh Token Redis 저장 및 쿠키 설정 완료");

                // 프론트엔드로 리다이렉트 (토큰 없이, 성공 상태만 전달하거나 그냥 리다이렉트)
                // 실제 운영 시에는 프론트엔드 주소로 변경해야 함 (예: http://localhost:3000/oauth2/redirect)
                String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/login/success")
                                .queryParam("status", "success")
                                .build().toUriString();

                getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
}
