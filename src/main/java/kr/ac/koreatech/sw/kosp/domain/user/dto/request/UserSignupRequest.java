package kr.ac.koreatech.sw.kosp.domain.user.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserSignupRequest(

    @Schema(description = "이름", example = "박성빈", requiredMode = REQUIRED)
    @NotBlank(message = "이름은 필수입니다.")
    @Pattern(regexp = "^(?:[가-힣]{2,5}(?: [가-힣]{2,5})?|[A-Za-z]+(?: [A-Za-z]+)*)$", message = "이름은 한글 2-5자 (띄어쓰기 1회 허용) 또는 영문 (띄어쓰기 허용)으로 입력해주세요.")
    String name,

    @Schema(description = "학번 또는 사번", example = "2023100514", requiredMode = REQUIRED)
    @NotBlank(message = "학번/사번은 필수입니다.")
    @Pattern(regexp = "^(\\d{10}|\\d{6}|\\d{8})$", message = "학번은 10자리, 사번은 6자리 또는 8자리 숫자여야 합니다.")
    String kutId,

    @Schema(description = "이메일", example = "kosp@koreatech.ac.kr", requiredMode = REQUIRED)
    @Size(max = 30, message = "이메일의 길이는 최대 30자 입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String kutEmail,

    @Schema(description = "비밀번호", example = "password123!", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", 
             message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다.")
    String password,

    @Schema(description = "회원가입 토큰 (JWS)", example = "eyJ...", requiredMode = REQUIRED)
    @NotBlank(message = "회원가입 토큰은 필수입니다.")
    String signupToken
) {
}
