package kr.ac.koreatech.sw.kosp.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 로그인한 사용자의 기본 정보 응답 DTO 임.")
public record AuthMeResponse(

    @Schema(description = "사용자 ID", example = "1")
    Long id,

    @Schema(description = "이메일", example = "user@koreatech.ac.kr")
    String email,

    @Schema(description = "사용자 이름", example = "홍길동")
    String name,

    @Schema(description = "프로필 이미지 URL", example = "https://avatars.githubusercontent.com/u/123456?v=4")
    String profileImage,

    @Schema(description = "자기소개", example = "안녕하세요")
    String introduction
) {
}
