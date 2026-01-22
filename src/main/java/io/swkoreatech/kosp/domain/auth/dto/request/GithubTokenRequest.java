package io.swkoreatech.kosp.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GithubTokenRequest(
    @NotBlank(message = "Github Access Token은 필수입니다.")
    String githubAccessToken
) {}
