package io.swkoreatech.kosp.common.repository;

import io.swkoreatech.kosp.common.entity.ProcessedMessage;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link ProcessedMessage} 엔티티의 데이터 접근 리포지토리.
 *
 * <p>메시지 중복 처리 방지를 위한 조회 및 저장 기능을 제공한다.</p>
 */
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, Long> {

    /**
     * 특정 메시지 ID의 처리 이력 존재 여부를 확인한다.
     *
     * @param messageId 메시지 고유 식별자
     * @return 이미 처리된 메시지이면 {@code true}
     */
    boolean existsByMessageId(String messageId);
}
