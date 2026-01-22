package io.swkoreatech.kosp.domain.upload.controller;

import io.swkoreatech.kosp.domain.upload.api.UploadApi;
import io.swkoreatech.kosp.domain.upload.dto.response.FileResponse;
import io.swkoreatech.kosp.domain.upload.service.UploadService;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UploadController implements UploadApi {

    private final UploadService uploadService;

    @Override
    @Permit(name = "upload:file", description = "파일 업로드")
    public ResponseEntity<FileResponse> uploadFile(MultipartFile file, User user) {
        return ResponseEntity.ok(uploadService.uploadFile(file, user));
    }
}
