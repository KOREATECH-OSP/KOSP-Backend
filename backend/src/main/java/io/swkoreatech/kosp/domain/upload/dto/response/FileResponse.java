package io.swkoreatech.kosp.domain.upload.dto.response;

import io.swkoreatech.kosp.domain.upload.model.Attachment;

import java.time.LocalDateTime;

public record FileResponse(
    Long id,
    String originalFileName,
    String url,
    Long fileSize,
    String contentType,
    LocalDateTime uploadedAt
) {
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
