package io.swkoreatech.kosp.global.auth.exception;

/**
 * JWT 토큰 클레임 파싱에 실패했을 때 발생하는 예외.
 */
public class TokenParseException extends RuntimeException {

    /**
     * 에러 메시지를 포함하는 예외를 생성한다.
     *
     * @param message 에러 메시지
     */
    public TokenParseException(String message) {
        super(message);
    }

    /**
     * 에러 메시지와 원인 예외를 포함하는 예외를 생성한다.
     *
     * @param message 에러 메시지
     * @param cause   원인 예외
     */
    public TokenParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
