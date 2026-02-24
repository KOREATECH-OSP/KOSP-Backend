package io.swkoreatech.kosp.common.repository;

import io.swkoreatech.kosp.common.entity.ProcessedMessage;

import org.springframework.data.repository.Repository;

public interface ProcessedMessageRepository extends Repository<ProcessedMessage, Long> {

    ProcessedMessage save(ProcessedMessage message);

    boolean existsByMessageId(String messageId);
}
