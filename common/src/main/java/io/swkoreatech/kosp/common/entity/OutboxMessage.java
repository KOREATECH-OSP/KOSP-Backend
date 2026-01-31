package io.swkoreatech.kosp.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "outbox_messages")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class OutboxMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 36)
    private String messageId;
    
    @Column(nullable = false)
    private String exchange;
    
    @Column(nullable = false)
    private String routingKey;
    
    @Column(nullable = false, length = 512)
    private String eventType;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;
    
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    
    public OutboxMessage(String messageId, String exchange, String routingKey, 
                         String eventType, String payload) {
        this.messageId = messageId;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
    
    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
    
    public void markFailed() {
        this.status = OutboxStatus.FAILED;
    }
}
