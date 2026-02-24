package com.walkdoro.domain.auth.controller;

import com.walkdoro.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import com.walkdoro.domain.auth.dto.ReissueResponse;
import com.walkdoro.domain.auth.dto.MessageResponse;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/v1/auth/reissue")
    public ResponseEntity<ReissueResponse> reissue(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {
        String accessToken = authService.reissueAccessToken(refreshToken);
        return ResponseEntity.ok(new ReissueResponse(accessToken));
    }

    @PostMapping("/api/v1/auth/logout")
    public ResponseEntity<MessageResponse> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            HttpServletResponse response) {

        String accessToken = null;
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            accessToken = bearerToken.substring(7);
        }

        authService.logout(refreshToken, accessToken);

        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료

        response.addCookie(cookie);

        return ResponseEntity.ok(new MessageResponse("LOGOUT_SUCCESS"));
    }
}
