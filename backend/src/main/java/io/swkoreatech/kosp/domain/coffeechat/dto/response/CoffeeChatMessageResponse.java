package io.swkoreatech.kosp.domain.coffeechat.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.coffeechat.document.CoffeeChatMessage;

public record CoffeeChatMessageResponse(
    String id,
    Long senderId,
    String content,
    LocalDateTime sentAt,
    boolean isRead
) {
    public static CoffeeChatMessageResponse from(CoffeeChatMessage msg) {
        return new CoffeeChatMessageResponse(
            msg.getId(),
            msg.getSenderId(),
            msg.getContent(),
            msg.getSentAt(),
            msg.isRead()
        );
    }
}
