package io.swkoreatech.kosp.global.auth.resolver;

import io.swkoreatech.kosp.global.auth.annotation.TokenSpec;
import io.swkoreatech.kosp.global.auth.token.JwtToken;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

/**
 * 토큰 클래스로부터 HTTP 헤더 이름을 결정하는 리졸버.
 * <p>{@link TokenSpec} 어노테이션의 토큰 타입을 기반으로
 * {@code X-{Type}-Token} 형식의 헤더 이름을 생성하며, 결과를 캐시한다.</p>
 */
@Component
public class TokenHeaderResolver {

    private final Map<Class<?>, String> headerNameCache = new ConcurrentHashMap<>();

    /**
     * 토큰 클래스에 붙은 {@link TokenSpec}을 읽어서 HTTP 헤더 이름을 결정한다.
     *
     * @param tokenClass 토큰 클래스
     * @return HTTP 헤더 이름 (예: {@code X-Access-Token})
     * @throws IllegalStateException {@code @TokenSpec} 어노테이션이 없는 경우
     */
    public String resolveHeaderName(Class<? extends JwtToken> tokenClass) {
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
