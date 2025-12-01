package kr.ac.koreatech.sw.kosp.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 로그인한 사용자의 기본 정보 응답 DTO 임.")
public record AuthMeResponse(

    @Schema(description = "사용자 이름", example = "홍길동")
    String name
) {
}
