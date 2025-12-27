package kr.ac.koreatech.sw.kosp.domain.community.article.controller;

import jakarta.validation.Valid;
import java.net.URI;
import kr.ac.koreatech.sw.kosp.domain.community.article.api.ArticleApi;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.service.BoardService;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.request.ArticleRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
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

import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/community/articles")
public class ArticleController implements ArticleApi {

    private final ArticleService articleService;
    private final BoardService boardService;

    @Override
    @GetMapping
    @Permit(permitAll = true, description = "게시글 목록 조회")
    public ResponseEntity<ArticleListResponse> getList(
        @AuthUser User user,
        @RequestParam Long boardId,
        Pageable pageable
    ) {
        Board board = boardService.getBoard(boardId);
        ArticleListResponse response = articleService.getList(board, pageable, user);
        return ResponseEntity.ok(response);
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
}
