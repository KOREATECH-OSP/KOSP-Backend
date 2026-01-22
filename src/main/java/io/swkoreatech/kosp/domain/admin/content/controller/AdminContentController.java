package io.swkoreatech.kosp.domain.admin.content.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.content.dto.request.NoticeUpdateRequest;
import io.swkoreatech.kosp.domain.admin.content.api.AdminContentApi;
import io.swkoreatech.kosp.domain.admin.content.dto.request.NoticeCreateRequest;
import io.swkoreatech.kosp.domain.admin.content.service.AdminContentService;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminContentController implements AdminContentApi {

    private final AdminContentService adminContentService;

    @Override
    @Permit(name = "admin:articles:delete", description = "게시글 삭제")
    public ResponseEntity<Void> deleteArticle(Long articleId) {
        adminContentService.deleteArticle(articleId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(name = "admin:notices:delete", description = "공지 삭제")
    public ResponseEntity<Void> deleteNotice(Long noticeId) {
        adminContentService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(name = "admin:notices:create", description = "공지사항 작성")
    public ResponseEntity<Void> createNotice(User user, NoticeCreateRequest request) {
        adminContentService.createNotice(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @Permit(name = "admin:notices:update", description = "공지사항 수정")
    public ResponseEntity<Void> updateNotice(Long noticeId, NoticeUpdateRequest request) {
        adminContentService.updateNotice(noticeId, request);
        return ResponseEntity.ok().build();
    }

}
