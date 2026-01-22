package io.swkoreatech.kosp.domain.upload.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UploadUrlRequest(
    @Schema(description = "파일 크기 (바이트)", example = "1024")
    Long contentLength,

    @Schema(description = "파일 타입", example = "image/png")
    String contentType,

    @Schema(description = "파일 이름", example = "profile.png")
    @NotBlank(message = "파일 이름은 필수입니다.")
    String fileName
) {
}
