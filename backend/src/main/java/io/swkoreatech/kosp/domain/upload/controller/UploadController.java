package io.swkoreatech.kosp.domain.upload.controller;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.upload.api.UploadApi;
import io.swkoreatech.kosp.domain.upload.dto.request.UploadUrlRequest;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadUrlResponse;
import io.swkoreatech.kosp.domain.upload.service.UploadService;
import io.swkoreatech.kosp.global.security.annotation.Permit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 파일 업로드 컨트롤러.
 * {@link UploadApi}의 구현체로, 파일 업로드 관련 요청을 처리한다.
 */
@RestController
@RequiredArgsConstructor
public class UploadController implements UploadApi {

    private final UploadService uploadService;

    /** {@inheritDoc} */
    @Override
    @Permit(name = "upload:url", description = "Presigned URL 생성")
    public ResponseEntity<UploadUrlResponse> getPresignedUrl(UploadUrlRequest request, User user) {
        return ResponseEntity.ok(uploadService.getPresignedUrl(request));
    }
}
