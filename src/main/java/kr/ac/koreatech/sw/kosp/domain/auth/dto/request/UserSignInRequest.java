package kr.ac.koreatech.sw.kosp.domain.auth.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSignInRequest(

    @Schema(description = "이메일", example = "kosp@koreatech.ac.kr", requiredMode = REQUIRED)
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @Schema(description = "비밀번호 (SHA 256 해싱된 값)", example = "cd06f8c2b0dd065faf6...", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 64, max = 64, message = "비밀번호 해시값은 16진수 64자여야 합니다.")
    String password
) {
}
