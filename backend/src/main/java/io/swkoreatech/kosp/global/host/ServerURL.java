package io.swkoreatech.kosp.global.host;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * 서버 URL을 컨트롤러 메서드 파라미터로 주입하기 위한 어노테이션.
 * <p>요청으로부터 서버의 스킴, 호스트, 포트를 조합하여 서버 URL을 추출한다.</p>
 */
@Hidden // Swagger 문서에 표시하지 않음
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface ServerURL {

}
