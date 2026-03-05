package io.swkoreatech.kosp.domain.upload.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.upload.dto.request.UploadUrlRequest;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadUrlResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

/**
 * 파일 업로드 API.
 * Presigned URL 생성을 통한 파일 업로드 기능을 정의한다.
 */
@Tag(name = "Upload", description = "파일 업로드 API")
@RequestMapping("/v1/upload")
public interface UploadApi {

    /**
     * S3에 직접 업로드할 수 있는 presigned URL을 생성한다.
     *
     * @param request 업로드 URL 요청
     * @param user 인증된 사용자
     * @return 업로드 URL 응답
     */
    @Operation(
        summary = "Presigned URL 생성",
        description = "S3에 직접 업로드할 수 있는 presigned URL을 생성합니다."
    )
    @ApiResponse(responseCode = "200", description = "URL 생성 성공")
    @PostMapping("/url")
    ResponseEntity<UploadUrlResponse> getPresignedUrl(
        @RequestBody @Valid UploadUrlRequest request,
        @AuthUser User user
    );
}
