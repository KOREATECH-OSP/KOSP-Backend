package io.swkoreatech.kosp.global.exception.handler;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.global.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 전역 예외 처리기.
 * <p>애플리케이션에서 발생하는 공통 예외를 가로채어 적절한 HTTP 응답으로 변환한다.
 * SSE 요청의 경우 빈 응답을, 일반 REST API 요청의 경우 JSON 에러 응답을 반환한다.</p>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 지원하지 않는 미디어 타입 예외를 처리한다.
     *
     * @param ex 미디어 타입 미지원 예외
     * @return 415 상태 코드의 에러 응답
     */
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

    /**
     * 애플리케이션 공통 예외를 처리한다.
     * <p>SSE 요청이면 빈 응답을, 일반 요청이면 JSON 에러 응답을 반환한다.</p>
     *
     * @param ex      글로벌 예외
     * @param request HTTP 요청 객체
     * @return 예외에 정의된 상태 코드의 응답
     */
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<?> handleGlobalException(
        GlobalException ex,
        HttpServletRequest request
    ) {
        String accept = request.getHeader("Accept");
        
        // SSE 요청: 빈 응답 반환
        if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            return ResponseEntity.status(ex.getStatus()).build();
        }
        
        // 일반 REST API: JSON 에러 응답
        ErrorResponse response = ErrorResponse.of(ex.getMessage(), ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * Bean Validation 예외를 처리한다.
     *
     * @param ex 유효성 검증 실패 예외
     * @return 400 상태 코드의 에러 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorResponse response = ErrorResponse.of(message, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 인증 예외를 처리한다.
     * <p>SSE 요청이면 빈 응답을, 일반 요청이면 JSON 에러 응답을 반환한다.</p>
     *
     * @param ex      인증 예외
     * @param request HTTP 요청 객체
     * @return 401 상태 코드의 응답
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(
        AuthenticationException ex,
        HttpServletRequest request
    ) {
        String accept = request.getHeader("Accept");
        
        // SSE 요청: 빈 응답 반환
        if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // 일반 REST API: JSON 에러 응답
        ErrorResponse response = ErrorResponse.of(
            ExceptionMessage.AUTHENTICATION.getMessage(),
            ExceptionMessage.AUTHENTICATION.getStatus().value()
        );
        return ResponseEntity.status(ExceptionMessage.AUTHENTICATION.getStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }
}
