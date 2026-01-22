package io.swkoreatech.kosp.domain.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.community.article.dto.response.ArticleListResponse;
import io.swkoreatech.kosp.domain.community.comment.dto.response.CommentListResponse;
import io.swkoreatech.kosp.domain.user.dto.response.GithubActivityResponse;
import io.swkoreatech.kosp.domain.user.api.UserActivityApi;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.service.UserActivityService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserActivityController implements UserActivityApi {

    private final UserActivityService userActivityService;

    @Override
    @Permit(permitAll = true, name = "users:posts:list", description = "사용자 작성 글 목록")
    public ResponseEntity<ArticleListResponse> getPosts(User user, Long userId, Pageable pageable) {
        return ResponseEntity.ok(userActivityService.getPosts(userId, pageable, user));
    }

    @Override
    @Permit(permitAll = true, name = "users:bookmarks:list", description = "사용자 즐겨찾기 목록")
    public ResponseEntity<ArticleListResponse> getBookmarks(User user, Long userId, Pageable pageable) {
        return ResponseEntity.ok(userActivityService.getBookmarks(userId, pageable, user));
    }

    @Override
    @Permit(permitAll = true, name = "users:comments:list", description = "사용자 작성 댓글 목록")
    public ResponseEntity<CommentListResponse> getComments(User user, Long userId, Pageable pageable) {
        return ResponseEntity.ok(userActivityService.getComments(userId, pageable, user));
    }

    @Override
    @Permit(permitAll = true, name = "users:github:activities", description = "사용자 활동 조회 (GitHub)")
    public ResponseEntity<GithubActivityResponse> getGithubActivities(User user, Long userId, Pageable pageable) {
        return ResponseEntity.ok(userActivityService.getGithubActivities(userId));
    }
}
