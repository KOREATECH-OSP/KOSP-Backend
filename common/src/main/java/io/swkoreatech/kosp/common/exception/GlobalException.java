package io.swkoreatech.kosp.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 애플리케이션 전역 예외 클래스.
 *
 * <p>HTTP 상태 코드를 포함하는 런타임 예외로,
 * 컨트롤러 어드바이스에서 적절한 HTTP 응답으로 변환된다.</p>
 */
@Getter
public class GlobalException extends RuntimeException {

    private final HttpStatus status;

    /**
     * 메시지와 HTTP 상태 코드로 예외를 생성한다.
     *
     * @param message 예외 메시지
     * @param status  HTTP 상태 코드
     */
    public GlobalException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * {@link ExceptionMessage} 열거형으로 예외를 생성한다.
     *
     * @param exceptionMessage 미리 정의된 예외 메시지
     */
    public GlobalException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage());
        this.status = exceptionMessage.getStatus();
    }
}
