package io.swkoreatech.kosp.domain.community.team.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 팀 초대 요청 DTO.
 *
 * @param email 초대할 사용자 이메일
 */
public record TeamInviteRequest(
    @Schema(description = "초대할 사용자의 KUT 이메일", example = "student@koreatech.ac.kr")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email
) {
}
