package io.swkoreatech.kosp.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그인 요청 DTO.
 *
 * @param email    이메일
 * @param password 비밀번호
 */
public record LoginRequest(

    @Schema(description = "이메일", example = "kosp@koreatech.ac.kr", requiredMode = REQUIRED)
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @Schema(description = "비밀번호", example = "password123!", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
) {
}
