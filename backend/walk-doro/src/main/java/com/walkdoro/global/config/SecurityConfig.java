package com.walkdoro.global.config;

import com.walkdoro.global.auth.handler.OAuth2AuthenticationSuccessHandler;
import com.walkdoro.global.auth.jwt.JwtAccessDeniedHandler;
import com.walkdoro.global.auth.jwt.JwtAuthenticationEntryPoint;
import com.walkdoro.domain.auth.repository.RefreshTokenRepository;
import com.walkdoro.global.auth.service.MyOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import com.walkdoro.global.auth.filter.JwtFilter;
import com.walkdoro.global.auth.jwt.JwtTokenProvider;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final MyOAuth2UserService myOAuth2UserService;
        private final JwtTokenProvider jwtTokenProvider;
        private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
        private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
        private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
        private final RefreshTokenRepository refreshTokenRepository;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable) // csrf 보호 비활성화
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                // H2 Console 사용을 위해
                                .headers(headers -> headers
                                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                                .sessionManagement((session) -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests((authorize) -> authorize
                                                .requestMatchers(PathRequest.toH2Console()).permitAll()
                                                .requestMatchers("/api/auth/reissue").permitAll()
                                                .requestMatchers("/api/auth/logout").permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                                .accessDeniedHandler(jwtAccessDeniedHandler))
                                .oauth2Login((oauth2) -> oauth2
                                                .userInfoEndpoint((userInfo) -> userInfo
                                                                .userService(myOAuth2UserService))
                                                .successHandler(oAuth2AuthenticationSuccessHandler))
                                .addFilterBefore(new JwtFilter(jwtTokenProvider, refreshTokenRepository),
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // 프론트엔드 주소 허용 (예: http://localhost:3000)
                // TODO: 배포 시 구체적인 도메인으로 변경 필요
                configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true); // 쿠키 수신을 위해 필수

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
