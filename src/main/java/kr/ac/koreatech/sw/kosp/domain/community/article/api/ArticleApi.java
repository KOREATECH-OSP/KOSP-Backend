package kr.ac.koreatech.sw.kosp.domain.community.article.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.request.ArticleRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Article", description = "게시글 관리 API")
public interface ArticleApi {

    @Operation(summary = "게시글 목록 조회", description = "전체 게시글 목록을 조회합니다.")
    @GetMapping
    ResponseEntity<ArticleListResponse> getList(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestParam Long boardId,
        @Parameter(hidden = true) Pageable pageable
    );

    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    ResponseEntity<ArticleResponse> getOne(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id
    );

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @PostMapping
    ResponseEntity<Void> create(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid ArticleRequest request
    );

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    @PutMapping("/{id}")
    ResponseEntity<Void> update(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id,
        @RequestBody @Valid ArticleRequest request
    );

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id
    );
    @Operation(summary = "게시글 북마크", description = "게시글 북마크를 토글합니다.")
    @PostMapping("/{id}/bookmarks")
    ResponseEntity<kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ToggleBookmarkResponse> toggleBookmark(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id
    );
}
