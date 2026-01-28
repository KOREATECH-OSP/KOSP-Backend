package io.swkoreatech.kosp.domain.admin.content.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swkoreatech.kosp.domain.admin.content.dto.request.NoticeCreateRequest;
import io.swkoreatech.kosp.domain.admin.content.dto.request.NoticeUpdateRequest;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

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

    @Operation(summary = "댓글 삭제", description = "관리자 권한으로 댓글을 삭제(Soft Delete)합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/comments/{commentId}")
    ResponseEntity<Void> deleteComment(@PathVariable Long commentId);

    @Operation(summary = "공지사항 작성", description = "관리자 권한으로 공지사항을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "작성 성공")
    @PostMapping("/notices")
    ResponseEntity<Void> createNotice(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid NoticeCreateRequest request
    );

    @Operation(summary = "공지사항 수정", description = "관리자 권한으로 공지사항을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    @PutMapping("/notices/{noticeId}")
    ResponseEntity<Void> updateNotice(
        @Parameter(description = "공지사항 ID") @PathVariable Long noticeId,
        @RequestBody @Valid NoticeUpdateRequest request
    );

}
