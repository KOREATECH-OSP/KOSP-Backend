package io.swkoreatech.kosp.common.repository;

import io.swkoreatech.kosp.common.entity.OutboxMessage;
import io.swkoreatech.kosp.common.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
