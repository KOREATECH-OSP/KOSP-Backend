package io.swkoreatech.kosp.global.security.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 약관 동의 여부를 강제하는 어노테이션.
 *
 * <p>{@link TermsAspect}에 의해 AOP 기반으로 검증된다.
 * 현재 약관에 동의하지 않은 사용자가 이 어노테이션이 붙은 메서드를 호출하면
 * {@code 403 Forbidden} 응답을 반환한다.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TermsRequired {
}
