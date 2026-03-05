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

/**
 * 처리 완료된 메시지 엔티티.
 *
 * <p>이벤트 기반 아키텍처에서 멱등성(idempotency)을 보장하기 위해
 * 이미 처리된 메시지 ID를 기록한다. 동일한 메시지의 중복 처리를 방지한다.</p>
 */
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

    /**
     * 처리된 메시지를 생성한다. 처리 시각은 현재 시각으로 자동 설정된다.
     *
     * @param messageId 메시지 고유 식별자
     * @param eventType 이벤트 유형
     */
    public ProcessedMessage(String messageId, String eventType) {
        this.messageId = messageId;
        this.eventType = eventType;
        this.processedAt = LocalDateTime.now();
    }
}
