package io.swkoreatech.kosp.domain.coffeechat.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Document(collection = "coffee_chat_messages")
@CompoundIndex(name = "room_sent_idx", def = "{'roomId': 1, 'sentAt': 1}")
public class CoffeeChatMessage {

    @Id
    private String id;

    private Long roomId;
    private Long senderId;
    private String content;

    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    @Builder.Default
    private boolean isRead = false;

    public void markAsRead() {
        this.isRead = true;
    }
}
