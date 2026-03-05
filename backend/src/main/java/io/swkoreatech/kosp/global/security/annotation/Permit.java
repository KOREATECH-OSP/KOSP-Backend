package io.swkoreatech.kosp.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 또는 타입 수준의 권한 제어 어노테이션.
 * <p>{@link io.swkoreatech.kosp.global.security.aop.PermissionAspect}에 의해
 * AOP 기반으로 권한을 검증한다. {@code permitAll}이 {@code true}이면 인증 없이 접근 가능하다.</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permit {

    /**
     * 필요한 권한 이름.
     *
     * @return 권한 이름 (빈 문자열이면 권한 검증 생략)
     */
    String name() default "";

    /**
     * 권한에 대한 설명.
     *
     * @return 권한 설명
     */
    String description() default "";

    /**
     * 모든 사용자에게 접근을 허용할지 여부.
     *
     * @return 모든 사용자 접근 허용 여부
     */
    boolean permitAll() default false;
}
