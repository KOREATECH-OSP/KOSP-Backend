package io.swkoreatech.kosp.global.auth.resolver;

import io.swkoreatech.kosp.global.auth.annotation.Token;
import io.swkoreatech.kosp.global.auth.exception.InvalidTokenException;
import io.swkoreatech.kosp.global.auth.token.JwtToken;

import jakarta.servlet.http.HttpServletRequest;

import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link Token} 어노테이션이 붙은 컨트롤러 메서드 파라미터에 JWT 토큰을 주입하는 리졸버.
 * <p>HTTP 요청 헤더에서 토큰 문자열을 추출하고, {@link JwtToken#from(Class, String)}을 통해
 * 검증 및 역직렬화하여 토큰 객체를 반환한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtArgumentResolver implements HandlerMethodArgumentResolver {

    private final TokenHeaderResolver tokenHeaderResolver;

    /** {@inheritDoc} */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Token.class)
            && JwtToken.class.isAssignableFrom(parameter.getParameterType());
    }

    /** {@inheritDoc} */
    @Override
    public Object resolveArgument(
        @NonNull MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        @NonNull NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new InvalidTokenException("HttpServletRequest not found");
        }

        // 1. 파라미터 클래스 타입 확인 (예: AccessToken.class)
        @SuppressWarnings("unchecked")
        Class<? extends JwtToken> tokenClass = (Class<? extends JwtToken>)parameter.getParameterType();

        // 2. 해당 클래스에 맞는 헤더 이름 찾기 (예: X-Access-Token)
        String headerName = tokenHeaderResolver.resolveHeaderName(tokenClass);

        // 3. 헤더에서 토큰 값 읽기
        String tokenString = request.getHeader(headerName);

        if (tokenString == null || tokenString.isBlank()) {
            throw new InvalidTokenException("Token header not found: " + headerName);
        }

        // 4. ✅ String → Token 객체 변환 (JwtToken.from()이 검증 수행)
        return JwtToken.from(tokenClass, tokenString);
    }
}
