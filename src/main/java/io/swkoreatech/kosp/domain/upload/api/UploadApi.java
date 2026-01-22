package io.swkoreatech.kosp.domain.upload.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.upload.dto.request.UploadUrlRequest;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadUrlResponse;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

@Tag(name = "Upload", description = "파일 업로드 API")
@RequestMapping("/v1/upload")
public interface UploadApi {

    @Operation(summary = "Presigned URL 생성", description = "S3에 직접 업로드할 수 있는 presigned URL을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "URL 생성 성공")
    @PostMapping("/url")
    ResponseEntity<UploadUrlResponse> getPresignedUrl(
        @RequestBody @Valid UploadUrlRequest request,
        @AuthUser User user
    );
}
