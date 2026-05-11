package io.swkoreatech.kosp.domain.resume.dto.response;

import java.time.LocalDateTime;

/**
 * 이력서 단건 조회 응답 DTO.
 *
 * @param resumeId   이력서 PK
 * @param userId     사용자 ID
 * @param isDefault  기본 이력서 여부
 * @param resumeData 이력서 전체 데이터 (JSON 객체, 없으면 null)
 * @param updatedAt  마지막 수정 시각
 */
public record ResumeResponse(
    Long resumeId,
    Long userId,
    boolean isDefault,
    Object resumeData,
    LocalDateTime updatedAt
) {
}
