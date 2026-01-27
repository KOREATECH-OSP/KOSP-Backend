package io.swkoreatech.kosp.common.trigger.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.trigger.model.CollectionTrigger;

public interface CollectionTriggerRepository extends Repository<CollectionTrigger, Long> {

    CollectionTrigger save(CollectionTrigger trigger);

    @Modifying
    @Query("""
        UPDATE CollectionTrigger t
        SET t.status = 'PROCESSING', t.processedAt = :now
        WHERE t.id IN (
            SELECT t2.id FROM CollectionTrigger t2
            WHERE t2.status = 'PENDING' AND t2.scheduledAt <= :now
            ORDER BY t2.priority ASC, t2.scheduledAt ASC
        )
        """)
    int claimPendingTriggers(LocalDateTime now);

    @Query("""
        SELECT t FROM CollectionTrigger t
        WHERE t.status = 'PROCESSING'
        ORDER BY t.priority ASC, t.scheduledAt ASC
        """)
    List<CollectionTrigger> findProcessing();
}
