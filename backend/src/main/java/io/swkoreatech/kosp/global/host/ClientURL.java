package io.swkoreatech.kosp.global.host;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * 클라이언트 URL을 컨트롤러 메서드 파라미터로 주입하기 위한 어노테이션.
 * <p>요청 헤더의 Origin 또는 Referer로부터 클라이언트 URL을 추출한다.</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(hidden = true)
public @interface ClientURL {
}
