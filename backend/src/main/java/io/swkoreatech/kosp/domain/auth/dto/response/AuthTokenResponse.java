package io.swkoreatech.kosp.domain.auth.dto.response;

/**
 * 인증 토큰 응답 DTO.
 *
 * @param accessToken  Access Token (JWT)
 * @param refreshToken Refresh Token (JWT)
 */
public record AuthTokenResponse(
    String accessToken,
    String refreshToken
) {
}
