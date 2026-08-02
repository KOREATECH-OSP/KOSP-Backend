package io.swkoreatech.kosp.domain.resume.dto.request;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 이력서 저장 요청 DTO.
 *
 * <p>프론트엔드 useResumeStorage 훅의 ResumeData 구조와 1:1 대응한다.
 * 각 필드는 null 허용 (미입력 상태로 저장 가능).</p>
 *
 * @param resumeTitle      이력서 제목
 * @param headline         한 줄 소개
 * @param bio              간단 소개
 * @param jobRole          개발 직무
 * @param techStack        기술 스택 목록
 * @param links            링크 목록
 * @param education        학력 목록
 * @param career           경력 목록
 * @param experience       교육이력 목록
 * @param projects         프로젝트 목록
 * @param awards           수상이력 목록
 * @param certifications   자격증 목록
 * @param coverLetters     자기소개서 목록
 * @param customSections   사용자 정의 섹션 목록
 * @param isPublic         공개 여부
 * @param visibleSections  섹션 표시 여부 맵 (key: 섹션 id, value: 표시 여부)
 */
public record ResumeSaveRequest(
    String resumeTitle,
    String headline,
    String bio,
    @JsonDeserialize(using = StringListDeserializer.class)
    List<String> jobRole,
    @JsonDeserialize(using = StringListDeserializer.class)
    List<String> techStack,
    List<LinkItem> links,
    List<EducationItem> education,
    List<CareerItem> career,
    List<ExperienceItem> experience,
    List<ProjectItem> projects,
    List<AwardItem> awards,
    List<CertificationItem> certifications,
    List<CoverLetterItem> coverLetters,
    List<CustomSectionItem> customSections,
    Boolean isPublic,
    Map<String, Object> visibleSections
) {

    // ── 중첩 레코드: 프론트 타입과 1:1 대응 ──────────────────────────

    public record LinkItem(String id, String label, String url) {}

    /**
     * 학력. {@code startDate}/{@code endDate} 가 정식 필드이며 형식은 {@code YYYY.MM.DD} 다.
     * {@code period} 는 과거 자유 입력 형식과의 호환 및 표시용으로 유지한다.
     */
    public record EducationItem(
        String id, String school, String major,
        String period, String startDate, String endDate
    ) {}

    /** 경력. 날짜 규칙은 {@link EducationItem} 과 동일하다. */
    public record CareerItem(
        String id, String company, String role,
        String period, String startDate, String endDate
    ) {}

    /** 교육이력. 날짜 규칙은 {@link EducationItem} 과 동일하다. */
    public record ExperienceItem(
        String id, String title, String description,
        String period, String startDate, String endDate
    ) {}

    /**
     * 프로젝트.
     *
     * <p>{@code techStack} 은 문자열 배열이다. 프론트엔드는 편집 시 콤마 구분 문자열로 다루지만
     * 전송(ResumePageClient.handleSave) 및 렌더링(ProjectCarousel) 시점에는 배열을 사용하므로
     * 저장 포맷의 기준은 배열이다. 과거 단일 문자열로 저장된 데이터와의 호환을 위해
     * {@code @JsonFormat(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)} 대신 커스텀 팩토리로 흡수한다.</p>
     */
    public record ProjectItem(
        String id,
        String name,
        String period,
        String startDate,
        String endDate,
        String summary,
        String role,
        @JsonDeserialize(using = StringListDeserializer.class)
        List<String> techStack,
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

    public record CustomFieldItem(String id, String label, String value) {}

    public record CustomSectionItem(String id, String title, List<CustomFieldItem> fields) {}
}
