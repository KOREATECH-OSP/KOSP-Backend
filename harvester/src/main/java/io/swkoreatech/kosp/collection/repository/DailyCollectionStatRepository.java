package io.swkoreatech.kosp.collection.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.swkoreatech.kosp.collection.document.DailyCollectionStatDocument;

/**
 * 일별 수집 통계 MongoDB 레포지토리.
 */
public interface DailyCollectionStatRepository extends MongoRepository<DailyCollectionStatDocument, String> {

    /**
     * 특정 사용자의 특정 날짜 이후 증분 수집 통계 목록을 반환한다.
     *
     * @param userId            사용자 ID
     * @param fromDate          조회 시작 날짜 (포함)
     * @param incrementalCollection 증분 수집 여부 필터
     * @return 수집 통계 목록
     */
    List<DailyCollectionStatDocument> findByUserIdAndCollectionDateGreaterThanEqualAndIncrementalCollection(
        Long userId, LocalDate fromDate, boolean incrementalCollection);
}
