package io.swkoreatech.kosp.domain.admin.content.api;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.dto.response.AdminArticleResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleListResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin - Article", description = "관리자 전용 게시글 관리 API")
@RequestMapping("/v1/admin/articles")
public interface AdminArticleApi {

    @Operation(
        summary = "관리자 게시글 목록 조회",
        description = "관리자 권한으로 특정 게시판의 게시글 목록을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<ArticleListResponse<AdminArticleResponse>> getList(
        @Parameter(hidden = true) @AuthUser User user,
        @Parameter(description = "게시판 ID") @RequestParam Long boardId,
        Pageable pageable
    );

    @Operation(
        summary = "관리자 게시글 상세 조회",
        description = "관리자 권한으로 게시글 상세 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    @GetMapping("/{id}")
    ResponseEntity<AdminArticleResponse> getOne(
        @Parameter(hidden = true) @AuthUser User user,
        @Parameter(description = "게시글 ID") @PathVariable Long id
    );
}
