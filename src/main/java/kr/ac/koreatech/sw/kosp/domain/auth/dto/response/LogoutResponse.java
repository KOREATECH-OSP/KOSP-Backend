package kr.ac.koreatech.sw.kosp.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LogoutResponse(
    
    @Schema(description = "로그아웃 메시지", example = "로그아웃 되었습니다.")
    String message
) {
}
