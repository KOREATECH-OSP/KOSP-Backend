package io.swkoreatech.kosp.domain.upload.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swkoreatech.kosp.domain.upload.api.UploadApi;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadResponse;
import io.swkoreatech.kosp.domain.upload.service.UploadService;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UploadController implements UploadApi {

    private final UploadService uploadService;

    @Override
    @Permit(name = "upload:file", description = "파일 업로드")
    public ResponseEntity<UploadResponse> upload(List<MultipartFile> files, User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(uploadService.upload(files));
    }
}
