package io.swkoreatech.kosp.global.exception.handler;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.global.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리기.
 * <p>애플리케이션에서 발생하는 공통 예외를 가로채어 적절한 HTTP 응답으로 변환한다.
 * SSE 요청의 경우 빈 응답을, 일반 REST API 요청의 경우 JSON 에러 응답을 반환한다.</p>
 */
@Slf4j
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

    /**
     * 매핑되지 않은 경로를 404 JSON 으로 응답한다.
     *
     * <p>이 처리기가 없으면 예외가 Spring Boot 기본 오류 처리({@code BasicErrorController})로 넘어가
     * 설정에 따라 스택트레이스나 Whitelabel 페이지가 노출된다. {@code spring.web.resources.add-mappings: false}
     * 라 정적 리소스 처리기가 없으므로 없는 경로는 {@link NoHandlerFoundException} 으로 온다.
     * 정적 리소스 매핑이 다시 켜질 경우를 대비해 {@link NoResourceFoundException} 도 함께 처리한다.</p>
     *
     * @return 404 상태 코드의 에러 응답 (요청 경로·예외 정보는 담지 않는다)
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(Exception ex) {
        return jsonError(ExceptionMessage.NOT_FOUND.getStatus(), ExceptionMessage.NOT_FOUND.getMessage());
    }

    /**
     * 위 처리기들이 잡지 못한 나머지 예외를 처리한다.
     *
     * <p>{@code @ExceptionHandler} 는 예외 계층에서 가장 가까운 타입의 처리기를 고르므로
     * {@code Exception.class} 인 이 처리기는 항상 마지막에 선택된다 (같은 클래스 안, 다른 ControllerAdvice 없음).</p>
     *
     * <p>응답 본문에는 예외 종류·메시지·스택을 절대 담지 않는다. 원인 추적은 로그로만 한다.
     * Spring MVC 표준 예외(405·400 등)는 원래 상태 코드를 유지해 500 으로 바뀌지 않게 한다.</p>
     *
     * @return 예외에 해당하는 상태 코드의 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex, HttpServletRequest request) {
        HttpStatusCode status = resolveStatus(ex);
        logUnexpected(ex, request, status);
        if (isEventStream(request)) {
            return ResponseEntity.status(status).build();
        }
        return jsonError(status, messageOf(status));
    }

    private HttpStatusCode resolveStatus(Exception ex) {
        if (ex instanceof org.springframework.web.ErrorResponse errorResponse) {
            return errorResponse.getStatusCode();
        }
        if (ex instanceof AccessDeniedException) {
            return ExceptionMessage.FORBIDDEN.getStatus();
        }
        ResponseStatus annotation = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        if (annotation != null) {
            return annotation.code();
        }
        return ExceptionMessage.SERVER_ERROR.getStatus();
    }

    private void logUnexpected(Exception ex, HttpServletRequest request, HttpStatusCode status) {
        if (status.is5xxServerError()) {
            log.error("처리되지 않은 예외: {} {}", request.getMethod(), request.getRequestURI(), ex);
            return;
        }
        log.warn("요청 처리 실패: {} {} → {} ({})", request.getMethod(), request.getRequestURI(),
            status.value(), ex.getClass().getSimpleName());
    }

    /** 상태 코드에 맞는 고정 문구. 예외 메시지는 쓰지 않는다. */
    private String messageOf(HttpStatusCode status) {
        if (status.is5xxServerError()) {
            return ExceptionMessage.SERVER_ERROR.getMessage();
        }
        if (status.value() == HttpStatus.NOT_FOUND.value()) {
            return ExceptionMessage.NOT_FOUND.getMessage();
        }
        if (status.value() == HttpStatus.FORBIDDEN.value()) {
            return ExceptionMessage.FORBIDDEN.getMessage();
        }
        return ExceptionMessage.BAD_REQUEST.getMessage();
    }

    private boolean isEventStream(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    private ResponseEntity<ErrorResponse> jsonError(HttpStatusCode status, String message) {
        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ErrorResponse.of(message, status.value()));
    }
}
