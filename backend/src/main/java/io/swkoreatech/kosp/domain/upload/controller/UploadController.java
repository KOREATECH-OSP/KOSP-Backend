package io.swkoreatech.kosp.domain.upload.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.upload.api.UploadApi;
import io.swkoreatech.kosp.domain.upload.dto.request.UploadUrlRequest;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadUrlResponse;
import io.swkoreatech.kosp.domain.upload.service.UploadService;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UploadController implements UploadApi {

    private final UploadService uploadService;

    @Override
    @Permit(name = "upload:url", description = "Presigned URL 생성")
    public ResponseEntity<UploadUrlResponse> getPresignedUrl(UploadUrlRequest request, User user) {
        return ResponseEntity.ok(uploadService.getPresignedUrl(request));
    }
}
