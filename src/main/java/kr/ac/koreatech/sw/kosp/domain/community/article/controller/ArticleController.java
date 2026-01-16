package kr.ac.koreatech.sw.kosp.domain.community.article.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.community.article.api.ArticleApi;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.request.ArticleRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ToggleBookmarkResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ToggleLikeResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.service.ArticleService;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.service.BoardService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/community/articles")
public class ArticleController implements ArticleApi {

    private final ArticleService articleService;
    private final BoardService boardService;

    @Override
    @GetMapping
    @Permit(permitAll = true, description = "게시글 목록 조회")
    public ResponseEntity<ArticleListResponse<ArticleResponse>> getList(
        @AuthUser User user,
        @RequestParam Long boardId,
        @RequestParam(required = false, defaultValue = "false") Boolean pinned,
        Pageable pageable
    ) {
        Board board = boardService.getBoard(boardId);
        if (Boolean.TRUE.equals(pinned)) {
            return ResponseEntity.ok(articleService.getPinnedList(board, pageable, user));
        }
        return ResponseEntity.ok(articleService.getList(board, pageable, user));
    }

    @Override
    @GetMapping("/{id}")
    @Permit(permitAll = true, description = "게시글 상세 조회")
    public ResponseEntity<ArticleResponse> getOne(
        @AuthUser User user,
        @PathVariable Long id
    ) {
        ArticleResponse response = articleService.getOne(id, user);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    @Permit(name = "article:create", description = "게시글 작성")
    public ResponseEntity<Void> create(
        @AuthUser User user,
        @RequestBody @Valid ArticleRequest request
    ) {
        Board board = boardService.getBoard(request.boardId());
        Long id = articleService.create(user, board, request);
        return ResponseEntity.created(URI.create("/v1/community/articles/" + id)).build();
    }

    @Override
    @PutMapping("/{id}")
    @Permit(name = "article:update", description = "게시글 수정")
    public ResponseEntity<Void> update(
        @AuthUser User user,
        @PathVariable Long id,
        @RequestBody @Valid ArticleRequest request
    ) {
        articleService.update(user, id, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/{id}")
    @Permit(name = "article:delete", description = "게시글 삭제")
    public ResponseEntity<Void> delete(
        @AuthUser User user,
        @PathVariable Long id
    ) {
        articleService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
    @Override
    @PostMapping("/{id}/likes")
    @Permit(name = "article:like", description = "게시글 좋아요")
    public ResponseEntity<ToggleLikeResponse> toggleLike(
        @AuthUser User user,
        @PathVariable Long id
    ) {
        boolean isLiked = articleService.toggleLike(user, id);
        return ResponseEntity.ok(new ToggleLikeResponse(isLiked));
    }

    @Override
    @PostMapping("/{id}/bookmarks")
    @Permit(name = "article:bookmark", description = "게시글 북마크")
    public ResponseEntity<ToggleBookmarkResponse> toggleBookmark(
        @AuthUser User user,
        @PathVariable Long id
    ) {
        boolean isBookmarked = articleService.toggleBookmark(user, id);
        return ResponseEntity.ok(new ToggleBookmarkResponse(isBookmarked));
    }
}
