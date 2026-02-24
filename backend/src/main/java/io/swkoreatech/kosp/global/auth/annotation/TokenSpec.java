package io.swkoreatech.kosp.global.auth.annotation;

import io.swkoreatech.kosp.global.auth.token.TokenType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TokenSpec {
    TokenType value();
}
