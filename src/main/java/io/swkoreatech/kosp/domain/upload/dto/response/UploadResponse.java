package io.swkoreatech.kosp.domain.upload.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record UploadResponse(
    @Schema(description = "업로드된 파일 URL 목록")
    List<String> urls
) {
    public static UploadResponse of(List<String> urls) {
        return new UploadResponse(urls);
    }

    public static UploadResponse of(String url) {
        return new UploadResponse(List.of(url));
    }
}
