package com.walkdoro.global.auth.service;

import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import com.walkdoro.global.auth.dto.OAuthAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class MyOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // DefaultOAuth2UserService를 이용해 UserInfo Endpoint에서 사용자 정보를 받아온다.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 어느 곳에서 OAuth2 받음?
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // user을 알려주는 AttributeName을 알아내기 위함
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, userNameAttributeName,
                oAuth2User.getAttributes());

        // 처음 회원가입인지 아니면 사용자 정보 업데이트
        User user = saveOrUpdate(oAuthAttributes);

        // Security Context에 저장하기 위해 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                oAuthAttributes.getAttributes(),
                oAuthAttributes.getNameAttributeKey());
    }

    // 소셜 서비스에서 가져온 사용자 정보를 저장하거나 업데이트
    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName())) // 이미 가입된 유저라면 이름(프로필)만 업데이트
                .orElse(attributes.toEntity()); // 가입되지 않은 유저라면 User 엔티티 생성

        return userRepository.save(user);
    }
}
