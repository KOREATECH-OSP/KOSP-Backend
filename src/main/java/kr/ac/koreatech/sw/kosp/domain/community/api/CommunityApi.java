package kr.ac.koreatech.sw.kosp.domain.community.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.community.dto.request.ArticleCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.dto.request.ArticleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.dto.response.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Community", description = "커뮤니티 게시글 관리 API")
@RequestMapping("/communities")
public interface CommunityApi {

    @GetMapping
    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 페이징하여 조회합니다.")
    ResponseEntity<Page<ArticleListResponse>> getArticles(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "latest") String sort
    );

    @GetMapping("/{articleId}")
    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    ResponseEntity<ArticleResponse> getArticle(@PathVariable Long articleId);

    @PostMapping
    @Operation(summary = "게시글 생성", description = "새로운 게시글을 작성합니다.")
    ResponseEntity<Void> createArticle(
        @Parameter(hidden = true) @AuthenticationPrincipal User user,
        @RequestBody @Valid ArticleCreateRequest request
    );

    @PutMapping("/{articleId}")
    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    ResponseEntity<Void> updateArticle(
        @Parameter(hidden = true) @AuthenticationPrincipal User user,
        @PathVariable Long articleId,
        @RequestBody @Valid ArticleUpdateRequest request
    );

    @DeleteMapping("/{articleId}")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    ResponseEntity<Void> deleteArticle(
        @Parameter(hidden = true) @AuthenticationPrincipal User user,
        @PathVariable Long articleId
    );
}
