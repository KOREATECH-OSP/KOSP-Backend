package io.swkoreatech.kosp.domain.resume.dto.response;

import io.swkoreatech.kosp.domain.material.model.MaterialSource;

/**
 * 이력서 자동 프로젝트 응답.
 *
 * <p>과제/EL 자료(MaterialItem, auto_imported=true)를 이력서 프로젝트 형태로 투영한 결과다.
 * 원본 자료에서 실시간 계산되므로 원본이 바뀌면 자동 반영된다.
 * 사용자가 수정({@code userEdited})했다면 overrides 가 원본 위에 덮여 반영된다.</p>
 *
 * <p>실제 필드 매핑(overrides 병합 포함)은
 * {@code ResumeAutoProjectService} 에서 수행한다(ObjectMapper 필요).</p>
 *
 * @param materialItemId       원본 자료 ID (삭제/복원/수정 키)
 * @param sourceType           출처 (AUNURI_ASSIGNMENT / AUNURI_EL)
 * @param name                 프로젝트명
 * @param period               기간 (연도 + 학기)
 * @param summary              요약 (과목명)
 * @param docLink              자료 링크
 * @param duplicatedWithGithub GitHub 프로젝트 중복 가능성 안내
 * @param duplicateRepoKey     중복 후보 repo (owner/repo)
 * @param autoImported         항상 true (자동 투영)
 * @param userEdited           사용자 수정본 여부
 * @param deletedByUser        사용자가 삭제(tombstone)했는지 여부 (복원 UI용)
 * @param isPublic             프로젝트 단위 공개 여부 (기본 비공개)
 */
public record ResumeAutoProjectResponse(
    Long materialItemId,
    MaterialSource sourceType,
    String name,
    String period,
    String summary,
    String docLink,
    boolean duplicatedWithGithub,
    String duplicateRepoKey,
    boolean autoImported,
    boolean userEdited,
    boolean deletedByUser,
    boolean isPublic
) {
}
