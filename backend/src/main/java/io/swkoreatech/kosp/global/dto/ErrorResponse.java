package io.swkoreatech.kosp.global.dto;

/**
 * 에러 응답 DTO.
 *
 * @param message 에러 메시지
 * @param status  HTTP 상태 코드
 */
public record ErrorResponse(String message, int status) {

    /**
     * 에러 메시지와 상태 코드로 에러 응답을 생성한다.
     *
     * @param message 에러 메시지
     * @param status  HTTP 상태 코드
     * @return 에러 응답 DTO
     */
    public static ErrorResponse of(String message, int status) {
        return new ErrorResponse(message, status);
    }
}
