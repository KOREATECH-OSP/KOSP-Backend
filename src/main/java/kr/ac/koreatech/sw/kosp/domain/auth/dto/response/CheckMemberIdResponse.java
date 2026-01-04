package kr.ac.koreatech.sw.kosp.domain.auth.dto.response;

public record CheckMemberIdResponse(
    boolean success,
    boolean available,
    String message
) {
}
