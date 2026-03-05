package io.swkoreatech.kosp.global.config.web;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import lombok.RequiredArgsConstructor;

/**
 * {@link AuthUser} 어노테이션이 붙은 컨트롤러 메서드 파라미터에 인증된 사용자를 주입하는 리졸버.
 * <p>SecurityContext에서 인증 정보를 가져와 {@link User} 객체를 반환한다.
 * 인증되지 않은 요청에서는 {@code null}을 반환한다.</p>
 */
@Component
@RequiredArgsConstructor
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    /** {@inheritDoc} */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class) &&
            User.class.isAssignableFrom(parameter.getParameterType());
    }

    /** {@inheritDoc} */
    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
        @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }

        return null;
    }
}
