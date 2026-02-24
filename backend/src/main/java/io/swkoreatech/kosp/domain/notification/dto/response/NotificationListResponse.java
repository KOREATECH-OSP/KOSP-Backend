package io.swkoreatech.kosp.domain.notification.dto.response;

import io.swkoreatech.kosp.domain.notification.model.Notification;

import java.util.List;

import org.springframework.data.domain.Page;

public record NotificationListResponse(
    List<NotificationResponse> notifications,
    long totalElements,
    int totalPages,
    int currentPage,
    int size
) {
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
