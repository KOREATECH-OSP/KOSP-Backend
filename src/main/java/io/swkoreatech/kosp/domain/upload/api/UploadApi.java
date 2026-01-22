package io.swkoreatech.kosp.domain.upload.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadResponse;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

@Tag(name = "Upload", description = "파일 업로드 API")
@RequestMapping("/v1/upload")
public interface UploadApi {

    @Operation(summary = "파일 업로드", description = "하나 이상의 파일을 S3에 업로드합니다.")
    @ApiResponse(responseCode = "201", description = "업로드 성공")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UploadResponse> upload(
        @Parameter(description = "업로드할 파일 (1개 이상)") @RequestPart("files") List<MultipartFile> files,
        @AuthUser User user
    );
}
