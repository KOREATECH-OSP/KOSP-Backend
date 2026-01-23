package io.swkoreatech.kosp.domain.auth.dto.response;

public record CheckMemberIdResponse(
    boolean success,
    boolean available,
    String message
) {
}
