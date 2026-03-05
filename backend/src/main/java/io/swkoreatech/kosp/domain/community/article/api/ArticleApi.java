package io.swkoreatech.kosp.domain.community.article.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.dto.request.ArticleRequest;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleListResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ToggleBookmarkResponse;
import io.swkoreatech.kosp.domain.community.article.dto.response.ToggleLikeResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

/**
 * 게시글 API 인터페이스.
 * 게시글의 CRUD, 좋아요, 북마크 엔드포인트를 정의한다.
 */
@Tag(name = "Community - Article", description = "게시글 관리 API")
public interface ArticleApi {

    /**
     * 게시글 목록을 조회한다.
     *
     * @param user 인증된 사용자
     * @param boardId 게시판 ID
     * @param pinned 고정 게시글만 조회 여부
     * @param pageable 페이징 정보
     * @return 게시글 목록 응답
     */
    @Operation(
        summary = "게시글 목록 조회",
        description = "전체 게시글 목록을 조회합니다. pinned=true로 고정 게시글(배너)만 조회할 수 있습니다."
    )
    @GetMapping
    ResponseEntity<ArticleListResponse<ArticleResponse>> getList(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestParam Long boardId,
        @RequestParam(required = false, defaultValue = "false") Boolean pinned,
        @Parameter(hidden = true) Pageable pageable
    );

    /**
     * 게시글 상세 정보를 조회한다.
     *
     * @param user 인증된 사용자
     * @param id 게시글 ID
     * @return 게시글 상세 응답
     */
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    ResponseEntity<ArticleResponse> getOne(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id
    );

    /**
     * 새로운 게시글을 작성한다.
     *
     * @param user 인증된 사용자
     * @param request 게시글 작성 요청
     * @return 생성 응답
     */
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @PostMapping
    ResponseEntity<Void> create(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid ArticleRequest request
    );

    /**
     * 게시글을 수정한다.
     *
     * @param user 인증된 사용자
     * @param id 게시글 ID
     * @param request 게시글 수정 요청
     * @return 수정 응답
     */
    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    @PutMapping("/{id}")
    ResponseEntity<Void> update(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id,
        @RequestBody @Valid ArticleRequest request
    );

    /**
     * 게시글을 삭제한다.
     *
     * @param user 인증된 사용자
     * @param id 게시글 ID
     * @return 삭제 응답
     */
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id
    );

    /**
     * 게시글 좋아요를 토글한다.
     *
     * @param user 인증된 사용자
     * @param id 게시글 ID
     * @return 좋아요 토글 응답
     */
    @Operation(summary = "게시글 좋아요", description = "게시글 좋아요를 토글합니다.")
    @PostMapping("/{id}/likes")
    ResponseEntity<ToggleLikeResponse> toggleLike(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id
    );

    /**
     * 게시글 북마크를 토글한다.
     *
     * @param user 인증된 사용자
     * @param id 게시글 ID
     * @return 북마크 토글 응답
     */
    @Operation(summary = "게시글 북마크", description = "게시글 북마크를 토글합니다.")
    @PostMapping("/{id}/bookmarks")
    ResponseEntity<ToggleBookmarkResponse> toggleBookmark(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id
    );
}
