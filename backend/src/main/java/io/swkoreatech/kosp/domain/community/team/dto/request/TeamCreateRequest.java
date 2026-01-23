package io.swkoreatech.kosp.domain.community.team.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record TeamCreateRequest(
    @NotBlank(message = "팀 이름은 필수입니다.")
    @Length(max = 50, message = "팀 이름은 50자 이하여야 합니다.")
    String name,

    @NotBlank(message = "팀 설명은 필수입니다.")
    String description,
    
    String imageUrl
) {
}
