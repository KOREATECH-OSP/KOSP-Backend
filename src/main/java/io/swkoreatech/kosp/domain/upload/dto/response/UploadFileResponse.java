package io.swkoreatech.kosp.domain.upload.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UploadFileResponse(
    @Schema(description = "업로드된 파일 URL", example = "https://kosp-bucket.s3.ap-northeast-2.amazonaws.com/upload/articles/2026/1/22/uuid/image.png")
    String fileUrl
) {
}
