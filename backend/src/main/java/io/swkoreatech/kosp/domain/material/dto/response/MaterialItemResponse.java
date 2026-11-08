package io.swkoreatech.kosp.domain.material.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.material.model.MaterialItem;
import io.swkoreatech.kosp.domain.material.model.MaterialSource;

/**
 * 학습자료 아이템 응답.
 *
 * @param id               자료 ID
 * @param folderId         소속 폴더 ID
 * @param title            자료명
 * @param subjectName      과목명
 * @param materialYear     연도
 * @param semester         학기
 * @param source           출처
 * @param sourceUrl        아우누리 원본 링크
 * @param fileUrl          파일 URL (S3). 타인 공개 조회에서는 항상 null 이며 다운로드 API를 사용한다.
 * @param originalFileName 원본 파일명
 * @param fileSize         파일 크기(byte)
 * @param contentType      MIME 타입
 * @param hasFile              내려받을 파일이 있는지 여부 (fileUrl 을 감춘 공개 응답에서 다운로드 버튼 노출 판단용)
 * @param isPublic             실제 적용되는 공개 여부 (아이템 override 없으면 폴더 설정 상속)
 * @param semesterOrder        최근학기 정렬키 (year*10 + term)
 * @param autoImported         자동 수집 여부 (확장/동기화로 들어온 자료)
 * @param duplicatedWithGithub GitHub 프로젝트와 중복 가능성 안내
 * @param duplicateRepoKey     중복 후보 repo (owner/repo)
 * @param materialDate         원본 게시/제출일
 * @param lastSyncedAt         마지막 동기화 시각
 * @param updatedAt            수정 시각
 */
public record MaterialItemResponse(
    Long id,
    Long folderId,
    String title,
    String subjectName,
    Integer materialYear,
    String semester,
    MaterialSource source,
    String sourceUrl,
    String fileUrl,
    String originalFileName,
    Long fileSize,
    String contentType,
    boolean hasFile,
    boolean isPublic,
    Integer semesterOrder,
    boolean autoImported,
    boolean duplicatedWithGithub,
    String duplicateRepoKey,
    LocalDateTime materialDate,
    LocalDateTime lastSyncedAt,
    LocalDateTime updatedAt
) {

    /**
     * 본인 조회용 응답. 모든 메타데이터를 그대로 노출한다.
     */
    public static MaterialItemResponse from(MaterialItem item) {
        return new MaterialItemResponse(
            item.getId(),
            item.getFolder().getId(),
            item.getTitle(),
            item.getSubjectName(),
            item.getMaterialYear(),
            item.getSemester(),
            item.getSource(),
            item.getSourceUrl(),
            item.getFileUrl(),
            item.getOriginalFileName(),
            item.getFileSize(),
            item.getContentType(),
            hasFile(item),
            item.isPublic(),
            item.getSemesterOrder(),
            item.isAutoImported(),
            item.isDuplicatedWithGithub(),
            item.getDuplicateRepoKey(),
            item.getMaterialDate(),
            item.getLastSyncedAt(),
            item.getUpdatedAt()
        );
    }

    /**
     * 타인 공개 조회용 응답.
     *
     * <p>S3 원본 URL({@code fileUrl})을 내보내지 않는다. 파일은 공개 다운로드 API가 발급하는
     * 만료 있는 presigned URL 로만 접근하게 하여, 응답에서 얻은 URL 을 무기한 재배포하지 못하게 한다.
     * 동기화 시각·GitHub 중복 판정 등 소유자용 내부 메타데이터도 함께 제외한다.</p>
     */
    public static MaterialItemResponse fromPublic(MaterialItem item) {
        return new MaterialItemResponse(
            item.getId(),
            item.getFolder().getId(),
            item.getTitle(),
            item.getSubjectName(),
            item.getMaterialYear(),
            item.getSemester(),
            item.getSource(),
            item.getSourceUrl(),
            null,
            item.getOriginalFileName(),
            item.getFileSize(),
            item.getContentType(),
            hasFile(item),
            true,
            item.getSemesterOrder(),
            item.isAutoImported(),
            false,
            null,
            item.getMaterialDate(),
            null,
            item.getUpdatedAt()
        );
    }

    private static boolean hasFile(MaterialItem item) {
        return item.getFileUrl() != null && !item.getFileUrl().isBlank();
    }
}
