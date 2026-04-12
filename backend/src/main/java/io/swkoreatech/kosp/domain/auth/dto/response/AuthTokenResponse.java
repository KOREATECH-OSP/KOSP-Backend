package io.swkoreatech.kosp.domain.auth.dto.response;

/**
 * 인증 토큰 응답 DTO.
 *
 * @param accessToken          Access Token (JWT)
 * @param refreshToken         Refresh Token (JWT)
 * @param needsTermsAgreement  현재 유효한 약관에 미동의 상태이면 {@code true}
 */
public record AuthTokenResponse(
    String accessToken,
    String refreshToken,
    boolean needsTermsAgreement
) {
}
