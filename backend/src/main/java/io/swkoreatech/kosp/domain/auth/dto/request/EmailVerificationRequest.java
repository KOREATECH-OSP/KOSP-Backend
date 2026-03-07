package io.swkoreatech.kosp.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 이메일 인증 코드 검증 요청 DTO.
 *
 * @param email 인증 대상 이메일
 * @param code  인증 코드
 */
public record EmailVerificationRequest(
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @NotBlank(message = "인증 코드는 필수입니다.")
    String code
) {
}
