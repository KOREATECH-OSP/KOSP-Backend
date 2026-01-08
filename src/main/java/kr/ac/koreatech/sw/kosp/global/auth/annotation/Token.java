package kr.ac.koreatech.sw.kosp.global.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Request Body에서 JWT 토큰 자동 추출 및 검증
 *
 * 사용 예시:
 * @Token RefreshToken refreshToken
 * → Body의 "refreshToken" 필드에서 자동 추출
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Token {
    /**
     * Request Body 필드명 (선택)
     * 기본값: 파라미터 변수명 사용
     */
    String value() default "";
}
