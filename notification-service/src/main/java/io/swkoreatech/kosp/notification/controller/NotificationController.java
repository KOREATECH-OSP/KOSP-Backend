package io.swkoreatech.kosp.notification.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swkoreatech.kosp.notification.api.NotificationApi;
import io.swkoreatech.kosp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    public SseEmitter subscribe(Long userId) {
        return notificationService.subscribe(userId);
    }
}
