package io.swkoreatech.kosp.domain.notification.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.notification.api.NotificationApi;
import io.swkoreatech.kosp.domain.notification.dto.response.NotificationListResponse;
import io.swkoreatech.kosp.domain.notification.dto.response.UnreadCountResponse;
import io.swkoreatech.kosp.domain.notification.service.NotificationService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 알림 컨트롤러.
 * {@link NotificationApi}의 구현체로, 알림 관련 요청을 처리한다.
 */
@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    /** {@inheritDoc} */
    @Override
    @Permit(description = "SSE 알림 구독")
    public SseEmitter subscribe(User user) {
        return notificationService.subscribe(user.getId());
    }

    /** {@inheritDoc} */
    @Override
    @Permit(description = "알림 목록 조회")
    public ResponseEntity<NotificationListResponse> getNotifications(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        NotificationListResponse response = notificationService.getNotifications(user, pageable);
        return ResponseEntity.ok(response);
    }

    /** {@inheritDoc} */
    @Override
    @Permit(description = "읽지 않은 알림 수 조회")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(User user) {
        return ResponseEntity.ok(notificationService.getUnreadCount(user));
    }

    /** {@inheritDoc} */
    @Override
    @Permit(description = "알림 읽음 처리")
    public ResponseEntity<Void> markAsRead(User user, Long notificationId) {
        notificationService.markAsRead(user, notificationId);
        return ResponseEntity.ok().build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(description = "알림 삭제")
    public ResponseEntity<Void> deleteNotification(User user, Long notificationId) {
        notificationService.deleteNotification(user, notificationId);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(description = "모든 알림 읽음 처리")
    public ResponseEntity<Void> markAllAsRead(User user) {
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(description = "모든 알림 삭제")
    public ResponseEntity<Void> deleteAll(User user) {
        notificationService.deleteAll(user);
        return ResponseEntity.noContent().build();
    }
}
