package io.swkoreatech.kosp.domain.admin.content.controller;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.admin.content.api.AdminArticleApi;
import io.swkoreatech.kosp.domain.community.article.dto.response.AdminArticleResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleListResponse;
import io.swkoreatech.kosp.domain.community.article.service.ArticleService;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.board.service.BoardService;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/articles")
public class AdminArticleController implements AdminArticleApi {

    private final ArticleService articleService;
    private final BoardService boardService;

    @Override
    @GetMapping
    @Permit(name = "admin:article:read", description = "관리자 게시글 목록 조회")
    public ResponseEntity<ArticleListResponse<AdminArticleResponse>> getList(
        @AuthUser User user,
        @RequestParam Long boardId,
        Pageable pageable
    ) {
        Board board = boardService.getBoard(boardId);
        ArticleListResponse<AdminArticleResponse> response = articleService.getListForAdmin(board, pageable, user);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{id}")
    @Permit(name = "admin:article:read", description = "관리자 게시글 상세 조회")
    public ResponseEntity<AdminArticleResponse> getOne(
        @AuthUser User user,
        @PathVariable Long id
    ) {
        AdminArticleResponse response = articleService.getOneForAdmin(id, user);
        return ResponseEntity.ok(response);
    }
}
