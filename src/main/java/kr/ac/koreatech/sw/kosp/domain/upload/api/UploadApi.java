package kr.ac.koreatech.sw.kosp.domain.upload.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.upload.dto.response.FileResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Upload", description = "파일 업로드 API")
@RequestMapping("/v1/files")
public interface UploadApi {

    @Operation(
        summary = "파일 업로드",
        description = "S3에 파일을 업로드하고 첨부파일 정보를 반환합니다. 반환된 ID를 사용하여 게시글 작성 시 첨부할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "업로드 성공")
    @PostMapping
    ResponseEntity<FileResponse> uploadFile(
        @Parameter(description = "업로드할 파일", required = true)
        @RequestParam("file") MultipartFile file,
        @AuthUser User user
    );
}
