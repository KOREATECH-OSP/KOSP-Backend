package io.swkoreatech.kosp.notification.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notification", description = "알림 API")
@RequestMapping("/v1/notifications")
public interface NotificationApi {

    @Operation(summary = "SSE 구독", description = "실시간 알림을 수신하기 위한 SSE 연결을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "연결 성공")
    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter subscribe(@Parameter(description = "사용자 ID") @PathVariable Long userId);
}

