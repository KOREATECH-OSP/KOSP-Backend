package io.swkoreatech.kosp.domain.notification.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.notification.dto.response.NotificationListResponse;
import io.swkoreatech.kosp.domain.notification.dto.response.UnreadCountResponse;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

@Tag(name = "Notification", description = "알림 API")
@RequestMapping("/v1/notifications")
public interface NotificationApi {

    @Operation(summary = "SSE 구독", description = "실시간 알림을 수신하기 위한 SSE 연결을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "연결 성공")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter subscribe(@Parameter(hidden = true) @AuthUser User user);

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 페이지네이션하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<NotificationListResponse> getNotifications(
        @Parameter(hidden = true) @AuthUser User user,
        @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "읽지 않은 알림 수 조회", description = "읽지 않은 알림의 개수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/unread-count")
    ResponseEntity<UnreadCountResponse> getUnreadCount(@Parameter(hidden = true) @AuthUser User user);

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음으로 처리합니다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @PostMapping("/{notificationId}/read")
    ResponseEntity<Void> markAsRead(
        @Parameter(hidden = true) @AuthUser User user,
        @Parameter(description = "알림 ID") @PathVariable Long notificationId
    );

    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{notificationId}")
    ResponseEntity<Void> deleteNotification(
        @Parameter(hidden = true) @AuthUser User user,
        @Parameter(description = "알림 ID") @PathVariable Long notificationId
    );

    @Operation(summary = "모든 알림 읽음 처리", description = "모든 알림을 읽음으로 처리합니다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @PostMapping("/read-all")
    ResponseEntity<Void> markAllAsRead(@Parameter(hidden = true) @AuthUser User user);

    @Operation(summary = "모든 알림 삭제", description = "모든 알림을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping
    ResponseEntity<Void> deleteAll(@Parameter(hidden = true) @AuthUser User user);
}
