package com.walkdoro.global.auth.annotation.loginUser;

import com.walkdoro.domain.user.User;
import com.walkdoro.global.error.ErrorCode;
import com.walkdoro.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @LoginUser 붙어있고
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
        // 파라미터 타입이 Long인 경우
        boolean isLongType = Long.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && isLongType;
    }

    @Override
    public @Nullable Object resolveArgument(MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == "anonymousUser") {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_USER);
        }

        // adapter를 사용해 principal을
        UserAdapter userAdapter = (UserAdapter) authentication.getPrincipal();

        return userAdapter.getId();
    }
}
