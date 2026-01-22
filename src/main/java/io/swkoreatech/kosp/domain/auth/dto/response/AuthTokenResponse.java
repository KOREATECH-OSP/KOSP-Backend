package io.swkoreatech.kosp.domain.auth.dto.response;

public record AuthTokenResponse(
    String accessToken,
    String refreshToken
) {
}
