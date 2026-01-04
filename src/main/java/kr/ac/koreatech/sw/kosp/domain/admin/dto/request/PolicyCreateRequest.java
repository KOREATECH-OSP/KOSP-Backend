package kr.ac.koreatech.sw.kosp.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PolicyCreateRequest(
    @NotBlank(message = "정책 이름은 필수입니다.")
    @Pattern(regexp = "^[A-Z][A-Za-z0-9]*$", message = "정책 이름은 대문자로 시작하는 PascalCase여야 합니다.")
    String name,

    String description
) {
}
