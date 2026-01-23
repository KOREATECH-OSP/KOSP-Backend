package io.swkoreatech.kosp.global.auth.exception;

/**
 * 토큰 검증 실패 예외
 */
import org.springframework.security.core.AuthenticationException;

/**
 * 토큰 검증 실패 예외
 */
public class InvalidTokenException extends AuthenticationException {
    
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
