package io.swkoreatech.kosp.domain.resume.dto.response;

import java.time.LocalDateTime;

/**
 * 이력서 조회 응답 DTO.
 *
 * <p>저장된 이력서 JSON 문자열을 그대로 포함하여 프론트엔드가
 * 파싱 후 즉시 초기값으로 사용할 수 있도록 한다.</p>
 *
 * @param userId     사용자 ID
 * @param resumeData 이력서 전체 데이터 (JSON 객체, 없으면 null)
 * @param updatedAt  마지막 수정 시각
 */
public record ResumeResponse(
    Long userId,
    Object resumeData,
    LocalDateTime updatedAt
) {
}
