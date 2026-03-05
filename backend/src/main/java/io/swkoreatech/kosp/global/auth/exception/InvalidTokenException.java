package io.swkoreatech.kosp.global.auth.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * JWT 토큰 검증에 실패했을 때 발생하는 예외.
 * <p>{@link AuthenticationException}을 상속하여 Spring Security 인증 실패로 처리된다.</p>
 */
public class InvalidTokenException extends AuthenticationException {

    /**
     * 에러 메시지를 포함하는 예외를 생성한다.
     *
     * @param message 에러 메시지
     */
    public InvalidTokenException(String message) {
        super(message);
    }

    /**
     * 에러 메시지와 원인 예외를 포함하는 예외를 생성한다.
     *
     * @param message 에러 메시지
     * @param cause   원인 예외
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
