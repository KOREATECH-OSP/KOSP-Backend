package io.swkoreatech.kosp.domain.upload.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.upload.model.Attachment;

/**
 * 파일 응답 DTO.
 *
 * @param id 첨부파일 ID
 * @param originalFileName 원본 파일명
 * @param url 파일 URL
 * @param fileSize 파일 크기
 * @param contentType 파일 MIME 타입
 * @param uploadedAt 업로드 일시
 */
public record FileResponse(
    Long id,
    String originalFileName,
    String url,
    Long fileSize,
    String contentType,
    LocalDateTime uploadedAt
) {
    /**
     * 첨부파일 엔티티로부터 응답을 생성한다.
     *
     * @param attachment 첨부파일
     * @return 파일 응답
     */
    public static FileResponse from(Attachment attachment) {
        return new FileResponse(
            attachment.getId(),
            attachment.getOriginalFileName(),
            attachment.getUrl(),
            attachment.getFileSize(),
            attachment.getContentType(),
            attachment.getUploadedAt()
        );
    }
}
