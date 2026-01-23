package io.swkoreatech.kosp.domain.upload.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record UploadUrlResponse(
    @Schema(description = "파일을 업로드할 수 있는 presigned URL")
    String preSignedUrl,

    @Schema(description = "업로드 완료 후 접근 가능한 파일 URL", example = "https://bucket.s3.region.amazonaws.com/upload/team/2025/1/22/uuid/image.png")
    String fileUrl,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "presigned URL 만료 일시", example = "2025-01-22 12:34:56")
    LocalDateTime expirationDate
) {

}
