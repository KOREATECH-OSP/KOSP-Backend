package io.swkoreatech.kosp.common.title.repository;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.title.model.TitleAdminLog;

/**
 * {@link TitleAdminLog} 엔티티 데이터 접근 레포지토리.
 */
public interface TitleAdminLogRepository extends Repository<TitleAdminLog, Long> {

    TitleAdminLog save(TitleAdminLog log);
}
