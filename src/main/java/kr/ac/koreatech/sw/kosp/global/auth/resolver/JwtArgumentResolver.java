package kr.ac.koreatech.sw.kosp.global.auth.resolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.thymeleaf.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import kr.ac.koreatech.sw.kosp.global.auth.annotation.Token;
import kr.ac.koreatech.sw.kosp.global.auth.annotation.TokenSpec;
import kr.ac.koreatech.sw.kosp.global.auth.exception.InvalidTokenException;
import kr.ac.koreatech.sw.kosp.global.auth.token.JwtToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Token 어노테이션 처리
 * Request Body에서 토큰 추출 및 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtArgumentResolver implements HandlerMethodArgumentResolver {

    private final Map<Class<?>, String> headerNameCache = new ConcurrentHashMap<>();

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Token.class)
            && JwtToken.class.isAssignableFrom(parameter.getParameterType());
    }

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
        String headerName = determineHeaderName(tokenClass);

        // 3. 헤더에서 토큰 값 읽기
        String tokenString = request.getHeader(headerName);

        if (tokenString == null || tokenString.isBlank()) {
            throw new InvalidTokenException("Token header not found: " + headerName);
        }

        // 4. ✅ String → Token 객체 변환 (JwtToken.from()이 검증 수행)
        return JwtToken.from(tokenClass, tokenString);
    }

    /**
     * 클래스에 붙은 @TokenSpec을 읽어서 헤더 이름을 결정
     */
    private String determineHeaderName(Class<? extends JwtToken> tokenClass) {
        return headerNameCache.computeIfAbsent(tokenClass, clazz -> {
            TokenSpec spec = clazz.getAnnotation(TokenSpec.class);
            if (spec == null) {
                throw new IllegalStateException(
                    "Token class " + clazz.getName() + " must be annotated with @TokenSpec"
                );
            }

            // Enum(ACCESS) -> Header(X-Access-Token) 변환
            return "X-" + StringUtils.capitalize(spec.value().toString()) + "-Token";
        });
    }
}
