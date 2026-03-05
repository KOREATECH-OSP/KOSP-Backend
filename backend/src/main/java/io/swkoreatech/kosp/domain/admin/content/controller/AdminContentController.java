package io.swkoreatech.kosp.domain.admin.content.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.admin.content.api.AdminContentApi;
import io.swkoreatech.kosp.domain.admin.content.dto.request.NoticeCreateRequest;
import io.swkoreatech.kosp.domain.admin.content.dto.request.NoticeUpdateRequest;
import io.swkoreatech.kosp.domain.admin.content.service.AdminContentService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 콘텐츠 관리 컨트롤러.
 * <p>{@link AdminContentApi}를 구현하여 게시글, 공지사항, 댓글 관리 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class AdminContentController implements AdminContentApi {

    private final AdminContentService adminContentService;

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:articles:delete", description = "게시글 삭제")
    public ResponseEntity<Void> deleteArticle(Long articleId) {
        adminContentService.deleteArticle(articleId);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:notices:delete", description = "공지 삭제")
    public ResponseEntity<Void> deleteNotice(Long noticeId) {
        adminContentService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:comments:delete", description = "댓글 삭제")
    public ResponseEntity<Void> deleteComment(Long commentId) {
        adminContentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:notices:create", description = "공지사항 작성")
    public ResponseEntity<Void> createNotice(User user, NoticeCreateRequest request) {
        adminContentService.createNotice(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:notices:update", description = "공지사항 수정")
    public ResponseEntity<Void> updateNotice(Long noticeId, NoticeUpdateRequest request) {
        adminContentService.updateNotice(noticeId, request);
        return ResponseEntity.ok().build();
    }

}
