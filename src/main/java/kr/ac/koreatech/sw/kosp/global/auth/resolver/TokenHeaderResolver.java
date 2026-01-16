package kr.ac.koreatech.sw.kosp.global.auth.resolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import kr.ac.koreatech.sw.kosp.global.auth.annotation.TokenSpec;
import kr.ac.koreatech.sw.kosp.global.auth.token.JwtToken;

@Component
public class TokenHeaderResolver {

    private final Map<Class<?>, String> headerNameCache = new ConcurrentHashMap<>();

    /**
     * 클래스에 붙은 @TokenSpec을 읽어서 헤더 이름을 결정
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
