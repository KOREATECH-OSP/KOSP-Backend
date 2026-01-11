package kr.ac.koreatech.sw.kosp.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailRequest(
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@koreatech\\.ac\\.kr$", message = "koreatech.ac.kr 이메일만 사용 가능합니다.")
    String email
) {
}
