package io.swkoreatech.kosp.domain.upload.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record UploadUrlRequest(
    @Schema(description = "파일 이름", example = "hello.png")
    @NotBlank(message = "파일 이름은 필수입니다.")
    String fileName,

    @Schema(description = "파일 크기 (bytes)", example = "1000")
    @NotNull(message = "파일 크기는 필수입니다.")
    Long contentLength,

    @Schema(description = "파일 타입", example = "image/png")
    @NotBlank(message = "파일 타입은 필수입니다.")
    String contentType
) {

}
