package io.swkoreatech.kosp.domain.material.dto.request;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.material.model.MaterialSource;
import io.swkoreatech.kosp.domain.material.model.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 학습자료 아이템 등록 요청.
 *
 * <p>파일은 프론트에서 presigned URL({@code POST /v1/upload/url})로 S3 업로드 후,
 * 반환된 {@code fileUrl}과 메타데이터를 함께 전달한다. 아우누리 링크만 등록할 수도 있다.</p>
 *
 * @param folderId         소속 폴더 ID
 * @param title            자료명
 * @param subjectName      과목명
 * @param materialYear     연도
 * @param semester         학기
 * @param source           출처 (null 이면 MANUAL)
 * @param sourceUrl        아우누리 원본 링크
 * @param fileUrl          업로드된 파일 URL (S3)
 * @param originalFileName 원본 파일명
 * @param fileSize         파일 크기(byte)
 * @param contentType      MIME 타입
 * @param visibility       아이템 단위 공개 override (null 이면 폴더 설정 상속)
 * @param materialDate     원본 게시/제출일 (최신순 정렬 기준, null 이면 등록 시각 사용)
 */
public record MaterialItemCreateRequest(
    @NotNull Long folderId,
    @NotBlank String title,
    String subjectName,
    Integer materialYear,
    String semester,
    MaterialSource source,
    String sourceUrl,
    String fileUrl,
    String originalFileName,
    Long fileSize,
    String contentType,
    Visibility visibility,
    LocalDateTime materialDate
) {
}
