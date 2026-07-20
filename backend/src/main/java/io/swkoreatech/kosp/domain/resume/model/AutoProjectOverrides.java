package io.swkoreatech.kosp.domain.resume.model;

/**
 * 자동 프로젝트 사용자 수정본(부분 override).
 *
 * <p>{@link ResumeMaterialProject#getOverrides()} JSONB 컬럼에 직렬화되어 저장되며,
 * 투영 시 원본 자료 값 위에 non-null 필드만 덮어쓴다.</p>
 */
public record AutoProjectOverrides(
    String name,
    String period,
    String summary,
    String docLink
) {
}
