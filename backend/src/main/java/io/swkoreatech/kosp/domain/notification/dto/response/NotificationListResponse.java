package io.swkoreatech.kosp.domain.notification.dto.response;

import io.swkoreatech.kosp.domain.notification.model.Notification;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * 알림 목록 응답 DTO.
 *
 * @param notifications 알림 응답 목록
 * @param totalElements 전체 알림 수
 * @param totalPages 전체 페이지 수
 * @param currentPage 현재 페이지 번호
 * @param size 페이지 크기
 */
public record NotificationListResponse(
    List<NotificationResponse> notifications,
    long totalElements,
    int totalPages,
    int currentPage,
    int size
) {
    /**
     * 알림 페이지로부터 알림 목록 응답을 생성한다.
     *
     * @param page 알림 페이지
     * @return 알림 목록 응답
     */
    public static NotificationListResponse from(Page<Notification> page) {
        List<NotificationResponse> notifications = page.getContent().stream()
            .map(NotificationResponse::from)
            .toList();

        return new NotificationListResponse(
            notifications,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }
}
