package io.swkoreatech.kosp.domain.coffeechat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.coffeechat.model.CoffeeChatRoom;
import io.swkoreatech.kosp.common.coffeechat.repository.CoffeeChatRoomRepository;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.coffeechat.document.CoffeeChatMessage;
import io.swkoreatech.kosp.domain.coffeechat.dto.request.CreateRoomRequest;
import io.swkoreatech.kosp.domain.coffeechat.dto.request.SendMessageRequest;
import io.swkoreatech.kosp.domain.coffeechat.dto.response.CoffeeChatMessageResponse;
import io.swkoreatech.kosp.domain.coffeechat.dto.response.CoffeeChatRoomResponse;
import io.swkoreatech.kosp.domain.coffeechat.dto.response.UnreadCountResponse;
import io.swkoreatech.kosp.domain.coffeechat.repository.CoffeeChatMessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoffeeChatService {

    private final CoffeeChatRoomRepository roomRepository;
    private final CoffeeChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    public List<CoffeeChatRoomResponse> getRooms(User me) {
        List<CoffeeChatRoom> rooms = roomRepository.findAllByUserId(me.getId());
        return rooms.stream()
            .map(room -> {
                long unread = messageRepository.countByRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), me.getId());
                return CoffeeChatRoomResponse.of(room, me.getId(), unread);
            })
            .sorted((a, b) -> Boolean.compare(b.isPinned(), a.isPinned()))
            .toList();
    }

    @Transactional
    public CoffeeChatRoomResponse createOrGetRoom(User me, CreateRoomRequest request) {
        if (me.getId().equals(request.partnerId())) {
            throw new GlobalException(ExceptionMessage.COFFEE_CHAT_SELF_MESSAGE);
        }

        User partner = userRepository.findById(request.partnerId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));

        CoffeeChatRoom room = roomRepository.findByParticipants(me.getId(), partner.getId())
            .orElseGet(() -> roomRepository.save(
                CoffeeChatRoom.builder()
                    .user1(me.getId() < partner.getId() ? me : partner)
                    .user2(me.getId() < partner.getId() ? partner : me)
                    .build()
            ));

        long unread = messageRepository.countByRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), me.getId());
        return CoffeeChatRoomResponse.of(room, me.getId(), unread);
    }

    public List<CoffeeChatMessageResponse> getMessages(User me, Long roomId) {
        CoffeeChatRoom room = roomRepository.getById(roomId);
        if (!room.isParticipant(me.getId())) {
            throw new GlobalException(ExceptionMessage.COFFEE_CHAT_NOT_PARTICIPANT);
        }
        return messageRepository.findByRoomIdOrderBySentAtAsc(roomId).stream()
            .map(CoffeeChatMessageResponse::from)
            .toList();
    }

    @Transactional
    public CoffeeChatMessageResponse sendMessage(User me, Long roomId, SendMessageRequest request) {
        CoffeeChatRoom room = roomRepository.getById(roomId);
        if (!room.isParticipant(me.getId())) {
            throw new GlobalException(ExceptionMessage.COFFEE_CHAT_NOT_PARTICIPANT);
        }

        CoffeeChatMessage message = messageRepository.save(
            CoffeeChatMessage.builder()
                .roomId(roomId)
                .senderId(me.getId())
                .content(request.content())
                .build()
        );

        room.updateLastMessage(request.content(), me.getId());
        roomRepository.save(room);

        return CoffeeChatMessageResponse.from(message);
    }

    @Transactional
    public void markMessagesAsRead(User me, Long roomId) {
        CoffeeChatRoom room = roomRepository.getById(roomId);
        if (!room.isParticipant(me.getId())) {
            throw new GlobalException(ExceptionMessage.COFFEE_CHAT_NOT_PARTICIPANT);
        }
        List<CoffeeChatMessage> unread = messageRepository.findByRoomIdAndSenderIdNotAndIsReadFalse(roomId, me.getId());
        unread.forEach(CoffeeChatMessage::markAsRead);
        messageRepository.saveAll(unread);
    }

    public UnreadCountResponse getTotalUnreadCount(User me) {
        List<CoffeeChatRoom> rooms = roomRepository.findAllByUserId(me.getId());
        long total = rooms.stream()
            .mapToLong(room -> messageRepository.countByRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), me.getId()))
            .sum();
        return UnreadCountResponse.of(total);
    }

    @Transactional
    public void deleteRoom(User me, Long roomId) {
        CoffeeChatRoom room = roomRepository.getById(roomId);
        if (!room.isParticipant(me.getId())) {
            throw new GlobalException(ExceptionMessage.COFFEE_CHAT_NOT_PARTICIPANT);
        }
        messageRepository.deleteByRoomId(roomId);
        roomRepository.delete(room);
    }

    @Transactional
    public CoffeeChatRoomResponse togglePin(User me, Long roomId) {
        CoffeeChatRoom room = roomRepository.getById(roomId);
        if (!room.isParticipant(me.getId())) {
            throw new GlobalException(ExceptionMessage.COFFEE_CHAT_NOT_PARTICIPANT);
        }
        room.togglePin();
        roomRepository.save(room);
        long unread = messageRepository.countByRoomIdAndSenderIdNotAndIsReadFalse(roomId, me.getId());
        return CoffeeChatRoomResponse.of(room, me.getId(), unread);
    }
}
