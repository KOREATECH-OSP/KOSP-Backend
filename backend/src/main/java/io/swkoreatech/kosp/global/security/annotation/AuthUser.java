package io.swkoreatech.kosp.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 인증된 사용자를 컨트롤러 메서드 파라미터로 주입하기 위한 어노테이션.
 * <p>Spring Security의 {@link AuthenticationPrincipal}을 래핑하여
 * SecurityContext로부터 인증된 {@link io.swkoreatech.kosp.common.user.model.User}를 추출한다.</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
public @interface AuthUser {
}
