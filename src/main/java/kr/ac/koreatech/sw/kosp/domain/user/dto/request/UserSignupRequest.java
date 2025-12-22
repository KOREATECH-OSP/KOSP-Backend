package kr.ac.koreatech.sw.kosp.domain.user.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public record UserSignupRequest(

    @Schema(description = "이름", example = "박성빈", requiredMode = REQUIRED)
    @NotBlank(message = "이름은 필수입니다.")
    @Pattern(regexp = "^(?:[가-힣]{2,5}|[A-Za-z]{2,30})$", message = "이름은 한글 2-5자 또는 영문 2-30자로 입력해주세요.")
    String name,

    @Schema(description = "학생 번호", example = "2023100514", requiredMode = REQUIRED)
    @NotBlank(message = "학번은 필수입니다.")
    String kutId,

    @Schema(description = "이메일", example = "kosp@koreatech.ac.kr", requiredMode = REQUIRED)
    @Size(max = 30, message = "이메일의 길이는 최대 30자 입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String kutEmail,

    @Schema(description = "비밀번호 (SHA 256 해싱된 값)", example = "cd06f8c2b0dd065faf6...", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 64, max = 64, message = "비밀번호 해시값은 16진수 64자여야 합니다.")
    String password,

    @Schema(description = "Github ID", example = "12345678", requiredMode = REQUIRED)
    @NotNull(message = "Github ID는 필수입니다.")
    Long githubId
) {

    public User toUser() {
        return User.builder()
            .name(name)
            .kutId(kutId)
            .kutEmail(kutEmail)
            .password(password)
            .build();
    }
}
