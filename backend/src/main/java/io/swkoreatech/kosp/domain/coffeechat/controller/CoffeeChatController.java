package io.swkoreatech.kosp.domain.coffeechat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.coffeechat.api.CoffeeChatApi;
import io.swkoreatech.kosp.domain.coffeechat.dto.request.CreateRoomRequest;
import io.swkoreatech.kosp.domain.coffeechat.dto.request.SendMessageRequest;
import io.swkoreatech.kosp.domain.coffeechat.dto.response.CoffeeChatMessageResponse;
import io.swkoreatech.kosp.domain.coffeechat.dto.response.CoffeeChatRoomResponse;
import io.swkoreatech.kosp.domain.coffeechat.dto.response.UnreadCountResponse;
import io.swkoreatech.kosp.domain.coffeechat.service.CoffeeChatService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CoffeeChatController implements CoffeeChatApi {

    private final CoffeeChatService coffeeChatService;

    @Override
    @Permit(description = "채팅방 목록 조회")
    public ResponseEntity<List<CoffeeChatRoomResponse>> getRooms(User user) {
        return ResponseEntity.ok(coffeeChatService.getRooms(user));
    }

    @Override
    @Permit(description = "채팅방 생성 또는 조회")
    public ResponseEntity<CoffeeChatRoomResponse> createOrGetRoom(User user, CreateRoomRequest request) {
        return ResponseEntity.ok(coffeeChatService.createOrGetRoom(user, request));
    }

    @Override
    @Permit(description = "메시지 목록 조회")
    public ResponseEntity<List<CoffeeChatMessageResponse>> getMessages(User user, Long roomId) {
        return ResponseEntity.ok(coffeeChatService.getMessages(user, roomId));
    }

    @Override
    @Permit(description = "메시지 전송")
    public ResponseEntity<CoffeeChatMessageResponse> sendMessage(User user, Long roomId, SendMessageRequest request) {
        return ResponseEntity.ok(coffeeChatService.sendMessage(user, roomId, request));
    }

    @Override
    @Permit(description = "메시지 읽음 처리")
    public ResponseEntity<Void> markAsRead(User user, Long roomId) {
        coffeeChatService.markMessagesAsRead(user, roomId);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(description = "읽지 않은 메시지 수")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(User user) {
        return ResponseEntity.ok(coffeeChatService.getTotalUnreadCount(user));
    }

    @Override
    @Permit(description = "채팅방 삭제")
    public ResponseEntity<Void> deleteRoom(User user, Long roomId) {
        coffeeChatService.deleteRoom(user, roomId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(description = "채팅방 고정/해제")
    public ResponseEntity<CoffeeChatRoomResponse> togglePin(User user, Long roomId) {
        return ResponseEntity.ok(coffeeChatService.togglePin(user, roomId));
    }
}
