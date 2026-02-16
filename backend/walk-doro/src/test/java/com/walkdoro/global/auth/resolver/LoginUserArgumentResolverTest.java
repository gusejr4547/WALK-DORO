package com.walkdoro.global.auth.resolver;

import com.walkdoro.global.auth.annotation.loginUser.LoginUser;
import com.walkdoro.global.auth.annotation.loginUser.UserAdapter;
import com.walkdoro.global.auth.annotation.loginUser.LoginUserArgumentResolver;
import com.walkdoro.global.error.ErrorCode;
import com.walkdoro.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserArgumentResolverTest {

    @InjectMocks
    private LoginUserArgumentResolver resolver;

    @Mock
    private MethodParameter parameter;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private ModelAndViewContainer mavContainer;

    @Mock
    private WebDataBinderFactory binderFactory;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("supportsParameter는 @LoginUser가 있고 Long 타입이면 true를 반환한다")
    void supportsParameter_ShouldReturnTrue_WhenConditionMet() {
        // given
        when(parameter.hasParameterAnnotation(LoginUser.class)).thenReturn(true);
        // doReturn(Long.class).when(parameter).getParameterType(); // Mockito specific
        // for Class<?> return types if needed, but standard when works for most
        // Note: Generic limitation with Mockito when syntax for Class<?> might be
        // tricky, but let's try standard when
        // Actually, getParameterType is not final, so we can mock it.
        // However, if we can't easily mock the Class return, we might need to rely on a
        // real MethodParameter or adjust mocking.
        // Let's assume standard mocking works for now.
        // We need to cast because getParameterType returns Class<?>
        when(parameter.getParameterType()).thenAnswer(invocation -> Long.class);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("인증된 사용자가 있으면 ID를 반환한다")
    void resolveArgument_ShouldReturnUserId_WhenAuthenticated() throws Exception {
        // given
        Long expectedId = 123L;
        UserAdapter userAdapter = mock(UserAdapter.class);
        when(userAdapter.getId()).thenReturn(expectedId);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userAdapter);

        // when
        Object result = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        // then
        assertThat(result).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("인증 정보가 없으면 예외를 던진다")
    void resolveArgument_ShouldThrowException_WhenAuthenticationIsNull() {
        // given
        when(securityContext.getAuthentication()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_USER);
    }

    @Test
    @DisplayName("익명 사용자이면 예외를 던진다")
    void resolveArgument_ShouldThrowException_WhenAnonymousUser() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_USER);
    }
}
