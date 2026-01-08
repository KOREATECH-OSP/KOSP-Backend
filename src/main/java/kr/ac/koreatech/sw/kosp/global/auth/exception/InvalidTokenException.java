package kr.ac.koreatech.sw.kosp.global.auth.exception;

/**
 * 토큰 검증 실패 예외
 */
public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
