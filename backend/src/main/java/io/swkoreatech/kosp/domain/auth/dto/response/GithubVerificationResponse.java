package io.swkoreatech.kosp.domain.auth.dto.response;

/**
 * GitHub 인증 응답 DTO.
 *
 * @param verificationToken 회원가입용 인증 토큰 (JWS)
 */
public record GithubVerificationResponse(
    String verificationToken
) {
    /**
     * 인증 토큰 문자열로부터 응답 DTO를 생성한다.
     *
     * @param verificationToken 회원가입용 인증 토큰
     * @return GitHub 인증 응답 DTO
     */
    public static GithubVerificationResponse from(String verificationToken) {
        return new GithubVerificationResponse(verificationToken);
    }
}
