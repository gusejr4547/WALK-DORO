package com.walkdoro.global.auth.service;

import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.repository.UserRepository;
import com.walkdoro.global.auth.dto.OAuthAttributes;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuthUserServiceTest {

    @InjectMocks
    private OAuthUserService oAuthUserService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("새 OAuth 이메일이면 유저를 생성한다")
    void saveOrUpdate_ShouldCreateUser_WhenEmailDoesNotExist() {
        OAuthAttributes attributes = attributes("new@example.com", "New User");
        User newUser = attributes.toEntity();

        given(userRepository.findByEmail("new@example.com")).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(newUser);

        User result = oAuthUserService.saveOrUpdate(attributes);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getName()).isEqualTo("New User");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("기존 OAuth 이메일이면 이름을 갱신한다")
    void saveOrUpdate_ShouldUpdateName_WhenEmailExists() {
        OAuthAttributes attributes = attributes("existing@example.com", "Changed Name");
        User existingUser = User.builder()
                .email("existing@example.com")
                .name("Old Name")
                .build();

        given(userRepository.findByEmail("existing@example.com")).willReturn(Optional.of(existingUser));
        given(userRepository.save(existingUser)).willReturn(existingUser);

        User result = oAuthUserService.saveOrUpdate(attributes);

        assertThat(result.getName()).isEqualTo("Changed Name");
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("OAuth 유저 저장은 하나의 트랜잭션으로 실행한다")
    void saveOrUpdate_ShouldBeTransactional() throws NoSuchMethodException {
        Method method = OAuthUserService.class.getMethod("saveOrUpdate", OAuthAttributes.class);

        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    private OAuthAttributes attributes(String email, String name) {
        return OAuthAttributes.builder()
                .attributes(Map.of("email", email, "name", name))
                .nameAttributeKey("sub")
                .email(email)
                .name(name)
                .build();
    }
}
