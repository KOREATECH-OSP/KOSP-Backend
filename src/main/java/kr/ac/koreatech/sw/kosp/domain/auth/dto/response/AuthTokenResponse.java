package kr.ac.koreatech.sw.kosp.domain.auth.dto.response;

public record AuthTokenResponse(
    String accessToken,
    String refreshToken
) {
}
