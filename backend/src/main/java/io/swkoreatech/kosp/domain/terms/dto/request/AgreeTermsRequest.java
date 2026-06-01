package io.swkoreatech.kosp.domain.terms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 약관 동의 요청 DTO.
 *
 * @param version 동의할 약관 버전
 */
public record AgreeTermsRequest(

    @Schema(description = "동의할 약관 버전", example = "1.0")
    @NotBlank(message = "약관 버전은 필수입니다.")
    String version
) {
}
