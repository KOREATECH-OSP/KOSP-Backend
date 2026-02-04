package io.swkoreatech.kosp.domain.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swkoreatech.kosp.domain.notification.dto.response.NotificationListResponse;
import io.swkoreatech.kosp.domain.notification.dto.response.NotificationResponse;
import io.swkoreatech.kosp.domain.notification.event.NotificationEvent;
import io.swkoreatech.kosp.domain.notification.model.Notification;
import io.swkoreatech.kosp.domain.notification.repository.NotificationRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final Long SSE_TIMEOUT = 60L * 60 * 1000;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> {
            if (isDisconnectError(e)) {
                log.debug("Client disconnected: user {}", userId);
            } else {
                log.error("SSE emitter error for user {}: {}", userId, e.getMessage(), e);
            }
            emitters.remove(userId);
        });

        sendToClient(userId, emitter, "connect", "SSE 연결 성공");

        return emitter;
    }

    @Transactional
    public void createAndSend(NotificationEvent event) {
        User user = findUser(event.getUserId());
        if (user == null) {
            return;
        }

        Notification notification = buildNotification(user, event);
        notificationRepository.save(notification);
        
        logNotificationCreation(user, notification);
        sendSseNotification(user.getId(), notification);
    }

    private User findUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found for notification: {}", userId);
        }
        return user;
    }

    private Notification buildNotification(User user, NotificationEvent event) {
        return Notification.builder()
            .user(user)
            .type(event.getType())
            .title(event.getTitle())
            .message(event.getMessage())
            .referenceId(event.getReferenceId())
            .build();
    }

    private void logNotificationCreation(User user, Notification notification) {
        log.info("Created notification for user {}: {}", user.getId(), notification.getTitle());
    }

    private void sendSseNotification(Long userId, Notification notification) {
        SseEmitter emitter = emitters.get(userId);

         if (emitter == null) {
             log.debug("No active SSE connection for user {}", userId);
             return;
         }

        NotificationResponse response = NotificationResponse.from(notification);
        sendToClient(userId, emitter, "notification", response);
    }

      private void sendToClient(Long userId, SseEmitter emitter, String eventName, Object data) {
          // Note: emitter.send() is async - errors are handled by onError() callback
          // try-catch here only catches synchronous errors (rare)
          try {
              emitter.send(SseEmitter.event()
                  .name(eventName)
                  .data(data));
          } catch (IOException e) {
              throw new RuntimeException(e);
          }
      }

    public NotificationListResponse getNotifications(User user, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        return NotificationListResponse.from(page);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(User user, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        validateOwnership(user, notification);

        notification.markAsRead();
    }

    @Transactional
    public void deleteNotification(User user, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        validateOwnership(user, notification);

        notificationRepository.delete(notification);
    }

    @Transactional
    public int markAllAsRead(User user) {
        return notificationRepository.markAllAsRead(user.getId());
    }

    @Transactional
    public int deleteAll(User user) {
        return notificationRepository.deleteAllByUserId(user.getId());
    }

    private void validateOwnership(User user, Notification notification) {
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }

    private boolean isDisconnectError(Throwable error) {
        if (error == null) {
            return false;
        }
        
        String className = error.getClass().getName();
        if (className.contains("AsyncRequestNotUsableException")) {
            return true;
        }
        
        if (error instanceof IOException) {
            return true;
        }
        
        // Check cause chain recursively
        return isDisconnectError(error.getCause());
    }

    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        // Errors are handled by onError() callback (async)
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
