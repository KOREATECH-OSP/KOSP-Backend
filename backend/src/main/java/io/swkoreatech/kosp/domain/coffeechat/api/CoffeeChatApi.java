package io.swkoreatech.kosp.domain.coffeechat.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.coffeechat.dto.request.CreateRoomRequest;
import io.swkoreatech.kosp.domain.coffeechat.dto.request.SendMessageRequest;
import io.swkoreatech.kosp.domain.coffeechat.dto.response.CoffeeChatMessageResponse;
import io.swkoreatech.kosp.domain.coffeechat.dto.response.CoffeeChatRoomResponse;
import io.swkoreatech.kosp.domain.coffeechat.dto.response.UnreadCountResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

@Tag(name = "CoffeeChat", description = "커피챗 API")
@RequestMapping("/v1/coffee-chat")
public interface CoffeeChatApi {

    @Operation(summary = "채팅방 목록 조회")
    @GetMapping("/rooms")
    ResponseEntity<List<CoffeeChatRoomResponse>> getRooms(@Parameter(hidden = true) @AuthUser User user);

    @Operation(summary = "채팅방 생성 또는 조회")
    @PostMapping("/rooms")
    ResponseEntity<CoffeeChatRoomResponse> createOrGetRoom(
        @Parameter(hidden = true) @AuthUser User user,
        @Valid @RequestBody CreateRoomRequest request
    );

    @Operation(summary = "메시지 목록 조회")
    @GetMapping("/rooms/{roomId}/messages")
    ResponseEntity<List<CoffeeChatMessageResponse>> getMessages(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long roomId
    );

    @Operation(summary = "메시지 전송")
    @PostMapping("/rooms/{roomId}/messages")
    ResponseEntity<CoffeeChatMessageResponse> sendMessage(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long roomId,
        @Valid @RequestBody SendMessageRequest request
    );

    @Operation(summary = "메시지 읽음 처리")
    @PostMapping("/rooms/{roomId}/read")
    ResponseEntity<Void> markAsRead(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long roomId
    );

    @Operation(summary = "읽지 않은 메시지 수")
    @GetMapping("/unread-count")
    ResponseEntity<UnreadCountResponse> getUnreadCount(@Parameter(hidden = true) @AuthUser User user);

    @Operation(summary = "채팅방 삭제")
    @DeleteMapping("/rooms/{roomId}")
    ResponseEntity<Void> deleteRoom(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long roomId
    );

    @Operation(summary = "채팅방 고정/해제")
    @PostMapping("/rooms/{roomId}/pin")
    ResponseEntity<CoffeeChatRoomResponse> togglePin(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long roomId
    );
}
