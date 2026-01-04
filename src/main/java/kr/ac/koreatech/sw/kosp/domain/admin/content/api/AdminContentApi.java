package kr.ac.koreatech.sw.kosp.domain.admin.content.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.admin.content.dto.request.NoticeCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Admin - Content", description = "관리자 전용 콘텐츠 관리 API")
@RequestMapping("/v1/admin")
public interface AdminContentApi {

    @Operation(summary = "게시글 삭제", description = "관리자 권한으로 게시글을 삭제(Soft Delete)합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/articles/{articleId}")
    ResponseEntity<Void> deleteArticle(@PathVariable Long articleId);

    @Operation(summary = "공지사항 삭제", description = "관리자 권한으로 공지사항을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/notices/{noticeId}")
    ResponseEntity<Void> deleteNotice(@PathVariable Long noticeId);

    @Operation(summary = "공지사항 작성", description = "관리자 권한으로 공지사항을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "작성 성공")
    @PostMapping("/notices")
    ResponseEntity<Void> createNotice(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid NoticeCreateRequest request
    );
}
