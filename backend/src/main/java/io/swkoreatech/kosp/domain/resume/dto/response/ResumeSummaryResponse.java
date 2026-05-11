package io.swkoreatech.kosp.domain.resume.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.resume.model.UserResume;

/**
 * 이력서 목록 조회 시 각 항목의 요약 응답 DTO.
 * resumeData는 포함하지 않아 트래픽을 줄인다.
 *
 * @param resumeId    이력서 PK
 * @param resumeTitle 이력서 제목 (resumeData 내 field, 없으면 null)
 * @param isDefault   기본 이력서 여부
 * @param isPublic    공개 여부
 * @param updatedAt   마지막 수정 시각
 */
public record ResumeSummaryResponse(
    Long resumeId,
    String resumeTitle,
    boolean isDefault,
    boolean isPublic,
    LocalDateTime updatedAt
) {

    @SuppressWarnings("unchecked")
    public static ResumeSummaryResponse from(UserResume resume, com.fasterxml.jackson.databind.ObjectMapper mapper) {
        String title = null;
        boolean isPublic = false;
        try {
            if (resume.getResumeData() != null) {
                java.util.Map<String, Object> map = mapper.readValue(resume.getResumeData(), java.util.Map.class);
                Object t = map.get("resumeTitle");
                if (t instanceof String s) title = s;
                Object pub = map.get("isPublic");
                isPublic = Boolean.TRUE.equals(pub);
            }
        } catch (Exception ignored) {}
        return new ResumeSummaryResponse(
            resume.getId(),
            title,
            resume.isDefault(),
            isPublic,
            resume.getUpdatedAt()
        );
    }
}
