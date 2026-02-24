package io.swkoreatech.kosp.domain.auth.dto.response;

public record CheckMemberIdResponse(
    boolean success,
    boolean available,
    String message
) {
    public static CheckMemberIdResponse from(boolean available, String message) {
        return new CheckMemberIdResponse(true, available, message);
    }
}
