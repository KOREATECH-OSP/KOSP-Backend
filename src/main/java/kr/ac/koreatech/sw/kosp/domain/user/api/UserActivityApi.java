package kr.ac.koreatech.sw.kosp.domain.user.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentListResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;

@Tag(name = "User Activity", description = "사용자 활동 조회 API")
@RequestMapping("/v1/users")
public interface UserActivityApi {

    @Operation(summary = "사용자 작성 글 목록", description = "사용자가 작성한 게시글 목록을 조회합니다.")
    @GetMapping("/{userId}/posts")
    ResponseEntity<ArticleListResponse> getPosts(
        @PathVariable Long userId,
        @Parameter(hidden = true) Pageable pageable
    );

    @Operation(summary = "사용자 작성 댓글 목록", description = "사용자가 작성한 댓글 목록을 조회합니다.")
    @GetMapping("/{userId}/comments")
    ResponseEntity<CommentListResponse> getComments(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId,
        @Parameter(hidden = true) Pageable pageable
    );
}
