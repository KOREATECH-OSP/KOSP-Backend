package kr.ac.koreatech.sw.kosp.global.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import kr.ac.koreatech.sw.kosp.global.dto.ErrorResponse;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException ex
    ) {
        ErrorResponse response = ErrorResponse.of(ex.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(GlobalException ex) {
        ErrorResponse response = ErrorResponse.of(ex.getMessage(), ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}
