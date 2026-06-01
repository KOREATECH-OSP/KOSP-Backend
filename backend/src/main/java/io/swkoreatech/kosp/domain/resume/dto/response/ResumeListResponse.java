package io.swkoreatech.kosp.domain.resume.dto.response;

import java.util.List;

/**
 * 이력서 목록 조회 응답 DTO.
 *
 * @param resumes    이력서 요약 목록
 * @param totalCount 전체 이력서 수
 */
public record ResumeListResponse(
    List<ResumeSummaryResponse> resumes,
    int totalCount
) {
}
