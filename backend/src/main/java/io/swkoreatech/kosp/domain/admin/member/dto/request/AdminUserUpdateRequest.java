package io.swkoreatech.kosp.domain.admin.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminUserUpdateRequest(
    @NotBlank(message = "이름은 필수입니다.")
    @Pattern(
        regexp = "^(?:[가-힣]{2,5}(?: [가-힣]{2,5})?|[A-Za-z]+(?: [A-Za-z]+)*)$",
        message = "이름은 한글 2-5자 (띄어쓰기 1회 허용) 또는 영문 (띄어쓰기 허용)으로 입력해주세요."
    )
    String name,

    @NotBlank(message = "학번/사번은 필수입니다.")
    @Pattern(
        regexp = "^(\\d{10}|\\d{6}|\\d{8})$",
        message = "학번은 10자리, 사번은 6자리 또는 8자리 숫자여야 합니다."
    )
    String kutId,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@koreatech\\.ac\\.kr$",
        message = "koreatech.ac.kr 이메일만 사용 가능합니다."
    )
    String kutEmail,

    String introduction,

    String profileImageUrl
) {
}
