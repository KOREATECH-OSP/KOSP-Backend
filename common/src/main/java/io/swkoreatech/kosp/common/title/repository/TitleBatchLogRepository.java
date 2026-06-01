package io.swkoreatech.kosp.common.title.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.title.model.TitleBatchLog;

/**
 * {@link TitleBatchLog} 엔티티 데이터 접근 레포지토리.
 */
public interface TitleBatchLogRepository extends Repository<TitleBatchLog, Long> {

    TitleBatchLog save(TitleBatchLog log);

    /**
     * 배치 실행 이력을 최신 순으로 페이지 조회한다.
     */
    Page<TitleBatchLog> findAllByOrderByExecutedAtDesc(Pageable pageable);

    /**
     * 최근 N건의 배치 실행 이력을 조회한다 (모니터링용).
     */
    List<TitleBatchLog> findTop5ByOrderByExecutedAtDesc();
}
