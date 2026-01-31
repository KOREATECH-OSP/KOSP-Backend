package io.swkoreatech.kosp.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swkoreatech.kosp.common.event.ChallengeCompletedEvent;
import io.swkoreatech.kosp.common.event.PointChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Long SSE_TIMEOUT = 60L * 60 * 1000;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        sendToClient(emitter, "connect", "SSE 연결 성공");

        return emitter;
    }

    public void sendChallengeNotification(ChallengeCompletedEvent event) {
        SseEmitter emitter = emitters.get(event.userId());

        if (emitter == null) {
            return;
        }

        String message = String.format("챌린지 '%s'를 완료했습니다! (%d점 획득)", 
            event.challengeName(), event.pointsAwarded());
        sendToClient(emitter, "challenge", message);
    }

    public void sendPointNotification(PointChangedEvent event) {
        SseEmitter emitter = emitters.get(event.userId());

        if (emitter == null) {
            return;
        }

        String message = String.format("포인트가 %d점 변경되었습니다. (사유: %s)", 
            event.amount(), event.reason());
        sendToClient(emitter, "point", message);
    }

    private void sendToClient(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                .name(eventName)
                .data(data));
        } catch (IOException e) {
            log.warn("Failed to send SSE event: {}", e.getMessage());
        }
    }
}
