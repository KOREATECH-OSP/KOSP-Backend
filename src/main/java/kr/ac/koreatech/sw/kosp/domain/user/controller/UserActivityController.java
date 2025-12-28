package kr.ac.koreatech.sw.kosp.domain.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.comment.dto.response.CommentListResponse;
import kr.ac.koreatech.sw.kosp.domain.user.api.UserActivityApi;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserActivityService;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserActivityController implements UserActivityApi {

    private final UserActivityService userActivityService;

    @Override
    @Permit(permitAll = true, description = "사용자 작성 글 목록")
    public ResponseEntity<ArticleListResponse> getPosts(Long userId, Pageable pageable) {
        return ResponseEntity.ok(userActivityService.getPosts(userId, pageable));
    }

    @Override
    @Permit(permitAll = true, description = "사용자 작성 댓글 목록")
    public ResponseEntity<CommentListResponse> getComments(User user, Long userId, Pageable pageable) {
        return ResponseEntity.ok(userActivityService.getComments(userId, pageable, user));
    }
}
