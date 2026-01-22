package io.swkoreatech.kosp.domain.upload.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.upload.dto.request.UploadUrlRequest;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadFileResponse;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadFilesResponse;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadUrlResponse;
import io.swkoreatech.kosp.domain.upload.model.ImageUploadDomain;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

@Tag(name = "Upload", description = "파일 업로드 API")
public interface UploadApi {

    @Operation(
        summary = "Presigned URL 발급",
        description = """
            S3에 직접 업로드할 수 있는 presigned URL을 발급합니다.
            
            {domain} 지원 목록:
            - articles: 게시글 첨부파일
            - profiles: 프로필 이미지
            - teams: 팀 이미지
            - recruits: 모집공고 이미지
            - admin: 관리자용
            """
    )
    @ApiResponse(responseCode = "200", description = "발급 성공")
    @PostMapping("/{domain}/upload/url")
    ResponseEntity<UploadUrlResponse> getPresignedUrl(
        @Parameter(description = "업로드 도메인") @PathVariable ImageUploadDomain domain,
        @RequestBody @Valid UploadUrlRequest request,
        @AuthUser User user
    );

    @Operation(
        summary = "단건 파일 업로드",
        description = """
            서버를 통해 S3에 파일을 업로드합니다.
            
            {domain} 지원 목록:
            - articles: 게시글 첨부파일
            - profiles: 프로필 이미지
            - teams: 팀 이미지
            - recruits: 모집공고 이미지
            - admin: 관리자용
            """
    )
    @ApiResponse(responseCode = "201", description = "업로드 성공")
    @PostMapping(
        value = "/{domain}/upload/file",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<UploadFileResponse> uploadFile(
        @Parameter(description = "업로드 도메인") @PathVariable ImageUploadDomain domain,
        @RequestPart("file") MultipartFile file,
        @AuthUser User user
    );

    @Operation(
        summary = "다건 파일 업로드",
        description = """
            서버를 통해 S3에 여러 파일을 업로드합니다.
            
            {domain} 지원 목록:
            - articles: 게시글 첨부파일
            - profiles: 프로필 이미지
            - teams: 팀 이미지
            - recruits: 모집공고 이미지
            - admin: 관리자용
            """
    )
    @ApiResponse(responseCode = "201", description = "업로드 성공")
    @PostMapping(
        value = "/{domain}/upload/files",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<UploadFilesResponse> uploadFiles(
        @Parameter(description = "업로드 도메인") @PathVariable ImageUploadDomain domain,
        @RequestPart("files") List<MultipartFile> files,
        @AuthUser User user
    );
}
