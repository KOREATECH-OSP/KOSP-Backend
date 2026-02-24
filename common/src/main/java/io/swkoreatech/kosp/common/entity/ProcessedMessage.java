package io.swkoreatech.kosp.common.entity;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_messages",
       indexes = @Index(name = "idx_message_id", columnList = "message_id"))
@Getter
@NoArgsConstructor(access = PROTECTED)
public class ProcessedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String messageId;

    private String eventType;
    private LocalDateTime processedAt;

    public ProcessedMessage(String messageId, String eventType) {
        this.messageId = messageId;
        this.eventType = eventType;
        this.processedAt = LocalDateTime.now();
    }
}
