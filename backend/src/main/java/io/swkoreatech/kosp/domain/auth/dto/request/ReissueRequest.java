package io.swkoreatech.kosp.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReissueRequest(
    @Schema(description = "Refresh Token", requiredMode = REQUIRED)
    @NotBlank(message = "Refresh Token은 필수입니다.")
    String refreshToken
) {
}
