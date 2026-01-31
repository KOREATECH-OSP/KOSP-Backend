package io.swkoreatech.kosp.common.repository;

import io.swkoreatech.kosp.common.entity.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, Long> {
    boolean existsByMessageId(String messageId);
}
