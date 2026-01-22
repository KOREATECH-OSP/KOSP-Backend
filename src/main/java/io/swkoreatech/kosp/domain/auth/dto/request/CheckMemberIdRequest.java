package io.swkoreatech.kosp.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CheckMemberIdRequest(
    @NotBlank(message = "아이디를 입력해주세요.")
    @Pattern(regexp = "^(\\d{10}|\\d{6}|\\d{8})$", message = "학번은 10자리, 사번은 6자리 또는 8자리 숫자여야 합니다.")
    @Schema(description = "학번 또는 사번", example = "2024000000")
    String id
) {
}
