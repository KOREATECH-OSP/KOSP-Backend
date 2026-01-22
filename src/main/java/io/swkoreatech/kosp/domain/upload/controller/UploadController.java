package io.swkoreatech.kosp.domain.upload.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swkoreatech.kosp.domain.upload.api.UploadApi;
import io.swkoreatech.kosp.domain.upload.dto.request.UploadUrlRequest;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadFileResponse;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadFilesResponse;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadUrlResponse;
import io.swkoreatech.kosp.domain.upload.model.ImageUploadDomain;
import io.swkoreatech.kosp.domain.upload.service.UploadService;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class UploadController implements UploadApi {

    private final UploadService uploadService;

    @Override
    @Permit(name = "upload:presigned-url", description = "Presigned URL 발급")
    public ResponseEntity<UploadUrlResponse> getPresignedUrl(
        @PathVariable ImageUploadDomain domain,
        UploadUrlRequest request,
        User user
    ) {
        return ResponseEntity.ok(uploadService.getPresignedUrl(domain, request));
    }

    @Override
    @Permit(name = "upload:file", description = "단건 파일 업로드")
    public ResponseEntity<UploadFileResponse> uploadFile(
        @PathVariable ImageUploadDomain domain,
        MultipartFile file,
        User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(uploadService.uploadFile(domain, file));
    }

    @Override
    @Permit(name = "upload:files", description = "다건 파일 업로드")
    public ResponseEntity<UploadFilesResponse> uploadFiles(
        @PathVariable ImageUploadDomain domain,
        List<MultipartFile> files,
        User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(uploadService.uploadFiles(domain, files));
    }
}
