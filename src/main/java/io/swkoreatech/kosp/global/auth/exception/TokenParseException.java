package io.swkoreatech.kosp.global.auth.exception;

/**
 * 토큰 파싱 실패 예외
 */
public class TokenParseException extends RuntimeException {
    
    public TokenParseException(String message) {
        super(message);
    }
    
    public TokenParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
