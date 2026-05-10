package io.swkoreatech.kosp.domain.resume.dto.request;

import java.util.List;

/**
 * 이력서 저장 요청 DTO.
 *
 * <p>프론트엔드 useResumeStorage 훅의 ResumeData 구조와 1:1 대응한다.
 * 각 필드는 null 허용 (미입력 상태로 저장 가능).</p>
 *
 * @param resumeTitle    이력서 제목
 * @param headline       한 줄 소개
 * @param bio            간단 소개
 * @param jobRole        개발 직무
 * @param techStack      기술 스택 목록
 * @param links          링크 목록
 * @param education      학력 목록
 * @param career         경력 목록
 * @param experience     교육이력 목록
 * @param projects       프로젝트 목록
 * @param awards         수상이력 목록
 * @param certifications 자격증 목록
 * @param coverLetters   자기소개서 목록
 * @param isPublic       공개 여부
 */
public record ResumeSaveRequest(
    String resumeTitle,
    String headline,
    String bio,
    String jobRole,
    List<String> techStack,
    List<LinkItem> links,
    List<EducationItem> education,
    List<CareerItem> career,
    List<ExperienceItem> experience,
    List<ProjectItem> projects,
    List<AwardItem> awards,
    List<CertificationItem> certifications,
    List<CoverLetterItem> coverLetters,
    Boolean isPublic
) {

    // ── 중첩 레코드: 프론트 타입과 1:1 대응 ──────────────────────────

    public record LinkItem(String id, String label, String url) {}

    public record EducationItem(String id, String school, String major, String period) {}

    public record CareerItem(String id, String company, String role, String period) {}

    public record ExperienceItem(String id, String title, String description, String period) {}

    public record ProjectItem(
        String id,
        String name,
        String period,
        String summary,
        String role,
        String techStack,
        String mainFeatures,
        String myContributions,
        String problemSolving,
        String result,
        String githubLink,
        String deployLink,
        String docLink,
        String featured
    ) {}

    public record AwardItem(
        String id,
        String name,
        String organization,
        String date,
        String relatedProject,
        String description
    ) {}

    /**
     * 자격증. status는 ACQUIRED 또는 EXPIRED만 허용.
     * 그 외 값(PREPARING, SCHEDULED 등)은 서비스 레이어에서 ACQUIRED로 보정.
     */
    public record CertificationItem(
        String id,
        String name,
        String organization,
        String date,
        String status
    ) {}

    public record CoverLetterItem(String id, String title, String content) {}
}
