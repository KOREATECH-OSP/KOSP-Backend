package io.swkoreatech.kosp.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {

    private final HttpStatus status;

    public GlobalException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public GlobalException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage());
        this.status = exceptionMessage.getStatus();
    }
}
