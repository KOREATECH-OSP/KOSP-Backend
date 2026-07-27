package io.swkoreatech.kosp.domain.coffeechat.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.coffeechat.model.CoffeeChatRoom;
import io.swkoreatech.kosp.common.user.model.User;

public record CoffeeChatRoomResponse(
    Long roomId,
    Long partnerId,
    String partnerName,
    String partnerGithubLogin,
    String partnerProfileImage,
    String lastMessage,
    LocalDateTime lastMessageAt,
    long unreadCount,
    boolean isPinned
) {
    public static CoffeeChatRoomResponse of(CoffeeChatRoom room, Long myId, long unreadCount) {
        User partner = room.getPartner(myId);
        String profileImage = partner.getGithubUser() != null ? partner.getGithubUser().getGithubAvatarUrl() : null;
        String githubLogin = partner.getGithubUser() != null ? partner.getGithubUser().getGithubLogin() : null;
        return new CoffeeChatRoomResponse(
            room.getId(),
            partner.getId(),
            partner.getName(),
            githubLogin,
            profileImage,
            room.getLastMessage(),
            room.getLastMessageAt(),
            unreadCount,
            room.isPinned()
        );
    }
}
