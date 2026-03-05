package io.swkoreatech.kosp.domain.user.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 사용자 정보 수정 요청 DTO.
 *
 * @param name 이름
 * @param introduction 자기소개
 */
public record UserUpdateRequest(
    @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
    String name,

    @Size(max = 255, message = "자기소개는 255자 이하여야 합니다.")
    String introduction
) {
}
