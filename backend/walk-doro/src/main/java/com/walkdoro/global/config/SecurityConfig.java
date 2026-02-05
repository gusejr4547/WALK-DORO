package com.walkdoro.global.config;

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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final MyOAuth2UserService myOAuth2UserService;
        private final JwtTokenProvider jwtTokenProvider;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable) // csrf 보호 비활성화
                                // H2 Console 사용을 위해
                                .headers(headers -> headers
                                                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                                .authorizeHttpRequests((authorize) -> authorize
                                                .requestMatchers(PathRequest.toH2Console()).permitAll()
                                                .requestMatchers("/api/auth/reissue").permitAll()
                                                .anyRequest().authenticated())
                                .oauth2Login((oauth2) -> oauth2
                                                .userInfoEndpoint((userInfo) -> userInfo
                                                                .userService(myOAuth2UserService)))
                                .addFilterBefore(new JwtFilter(jwtTokenProvider),
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
