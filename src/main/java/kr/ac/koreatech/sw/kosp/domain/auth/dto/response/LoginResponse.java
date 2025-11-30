package kr.ac.koreatech.sw.kosp.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
    
    @Schema(description = "사용자 타입", example = "student")
    String userType
) {
}
