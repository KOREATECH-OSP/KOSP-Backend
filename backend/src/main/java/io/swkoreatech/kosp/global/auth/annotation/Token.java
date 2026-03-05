package io.swkoreatech.kosp.global.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 메서드 파라미터에서 JWT 토큰을 추출하기 위한 어노테이션.
 * <p>{@link io.swkoreatech.kosp.global.auth.resolver.JwtArgumentResolver}에 의해 처리되며,
 * HTTP 헤더에서 토큰을 추출하여 {@link io.swkoreatech.kosp.global.auth.token.JwtToken}
 * 하위 클래스 인스턴스로 변환한다.</p>
 * <p>사용법: {@code @Token AccessToken accessToken}</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Token {
}
