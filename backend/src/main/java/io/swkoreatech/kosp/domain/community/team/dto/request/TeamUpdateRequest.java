package io.swkoreatech.kosp.domain.community.team.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamUpdateRequest(
    @Schema(description = "팀 이름", example = "KOSP Team")
    @NotBlank(message = "팀 이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "팀 이름은 2자 이상 50자 이하로 입력해주세요.")
    String name,

    @Schema(description = "팀 설명", example = "KOSP 프로젝트를 진행하는 팀입니다.")
    @NotBlank(message = "팀 설명은 필수입니다.")
    String description,

    @Schema(description = "팀 이미지 URL", example = "https://example.com/image.png")
    String imageUrl
) {
}
