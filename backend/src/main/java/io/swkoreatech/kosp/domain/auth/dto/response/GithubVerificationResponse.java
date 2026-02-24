package io.swkoreatech.kosp.domain.auth.dto.response;

public record GithubVerificationResponse(
    String verificationToken
) {
    public static GithubVerificationResponse from(String verificationToken) {
        return new GithubVerificationResponse(verificationToken);
    }
}
