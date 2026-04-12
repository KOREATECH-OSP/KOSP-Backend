package io.swkoreatech.kosp.domain.season.mongo;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 시즌 커밋 점수 계산용 MongoDB 리포지토리.
 *
 * <p>harvester가 수집한 {@code github_commits} 컬렉션을 읽기 전용으로 조회한다.</p>
 */
public interface SeasonCommitRepository extends MongoRepository<SeasonCommitDocument, String> {

    /**
     * 특정 사용자의 기간 내 커밋 목록을 조회한다 (일별 cap 계산용).
     */
    List<SeasonCommitDocument> findByUserIdAndAuthoredAtBetween(Long userId, Instant start, Instant end);
}
