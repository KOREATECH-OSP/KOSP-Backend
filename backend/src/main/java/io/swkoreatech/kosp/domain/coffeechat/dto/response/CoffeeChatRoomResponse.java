package io.swkoreatech.kosp.domain.coffeechat.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.coffeechat.model.CoffeeChatRoom;
import io.swkoreatech.kosp.common.user.model.User;

public record CoffeeChatRoomResponse(
    Long roomId,
    Long partnerId,
    String partnerName,
    String partnerProfileImage,
    String lastMessage,
    LocalDateTime lastMessageAt,
    long unreadCount
) {
    public static CoffeeChatRoomResponse of(CoffeeChatRoom room, Long myId, long unreadCount) {
        User partner = room.getPartner(myId);
        return new CoffeeChatRoomResponse(
            room.getId(),
            partner.getId(),
            partner.getName(),
            partner.getProfileImage(),
            room.getLastMessage(),
            room.getLastMessageAt(),
            unreadCount
        );
    }
}
