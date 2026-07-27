package io.swkoreatech.kosp.common.coffeechat.model;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "coffee_chat_rooms",
    uniqueConstraints = @UniqueConstraint(name = "uq_coffee_chat_users", columnNames = {"user1_id", "user2_id"})
)
public class CoffeeChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(name = "last_message")
    private String lastMessage;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_sender_id")
    private Long lastSenderId;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @Builder
    private CoffeeChatRoom(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    public void updateLastMessage(String message, Long senderId) {
        this.lastMessage = message;
        this.lastMessageAt = LocalDateTime.now();
        this.lastSenderId = senderId;
    }

    public User getPartner(Long myId) {
        return user1.getId().equals(myId) ? user2 : user1;
    }

    public boolean isParticipant(Long userId) {
        return user1.getId().equals(userId) || user2.getId().equals(userId);
    }

    public void togglePin() {
        this.isPinned = !this.isPinned;
    }
}
