package io.swkoreatech.kosp.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 비밀번호 재설정 요청 DTO.
 *
 * @param token       비밀번호 재설정 토큰
 * @param newPassword 새 비밀번호 (영문, 숫자, 특수문자 포함 8자 이상)
 */
public record PasswordResetRequest(
    @NotBlank(message = "토큰은 필수입니다.")
    String token,

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."
    )
    String newPassword
) {
}
