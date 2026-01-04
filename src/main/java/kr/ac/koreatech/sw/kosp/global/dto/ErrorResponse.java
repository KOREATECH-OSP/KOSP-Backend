package kr.ac.koreatech.sw.kosp.global.dto;

public record ErrorResponse(String message, int status) {
    public static ErrorResponse of(String message, int status) {
        return new ErrorResponse(message, status);
    }
}
