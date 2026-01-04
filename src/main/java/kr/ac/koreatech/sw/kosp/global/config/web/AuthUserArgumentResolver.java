package kr.ac.koreatech.sw.kosp.global.config.web;

import jakarta.servlet.http.HttpServletRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.auth.provider.LoginTokenProvider;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;

import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.jsonwebtoken.Claims;
import kr.ac.koreatech.sw.kosp.global.auth.core.AuthToken;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final LoginTokenProvider loginTokenProvider;
    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class) &&
               User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
        @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }

        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        
        try {
            // Parse JWT and get user ID from subject
            AuthToken<Claims> authToken = loginTokenProvider.convertAuthToken(token);
            if (!authToken.validate()) {
                return null;
            }
            
            Claims claims = authToken.getData();
            String userId = claims.getSubject();
            
            // Query User from DB by ID
            return userRepository.findById(Long.parseLong(userId))
                .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
