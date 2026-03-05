package io.swkoreatech.kosp.global.auth.annotation;

import io.swkoreatech.kosp.global.auth.token.TokenType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JWT 토큰 클래스에 토큰 타입을 지정하는 어노테이션.
 * <p>{@link JwtToken}의 하위 클래스에 적용하여 해당 토큰의 유형(ACCESS, REFRESH, SIGNUP)을 명시한다.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TokenSpec {

    /**
     * 토큰 타입을 지정한다.
     *
     * @return 토큰 타입
     */
    TokenType value();
}
