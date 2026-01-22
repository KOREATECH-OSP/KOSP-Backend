package io.swkoreatech.kosp.domain.upload.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record UploadFilesResponse(
    @Schema(description = "업로드된 파일 URL 목록")
    List<String> fileUrls
) {
}
