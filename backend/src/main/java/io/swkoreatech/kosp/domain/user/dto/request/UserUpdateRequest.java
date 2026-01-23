package io.swkoreatech.kosp.domain.user.dto.request;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    String name,

    @Size(max = 255, message = "자기소개는 255자 이하여야 합니다.")
    String introduction
) {
}
