package io.swkoreatech.kosp.collection.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.swkoreatech.kosp.collection.document.CommitDocument;

/**
 * 커밋 문서 MongoDB 리포지토리.
 *
 * <p>GitHub 커밋 데이터에 대한 CRUD 및 사용자/저장소별 조회 기능을 제공한다.
 */
public interface CommitDocumentRepository extends MongoRepository<CommitDocument, String> {

    /**
     * 사용자 ID로 커밋 문서 목록을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 커밋 문서 목록
     */
    List<CommitDocument> findByUserId(Long userId);

    /**
     * 사용자 ID와 저장소 이름으로 커밋 문서 목록을 조회한다.
     *
     * @param userId         사용자 ID
     * @param repositoryName 저장소 이름
     * @return 커밋 문서 목록
     */
    List<CommitDocument> findByUserIdAndRepositoryName(Long userId, String repositoryName);

    /**
     * 사용자 ID와 SHA로 커밋 문서 존재 여부를 확인한다.
     *
     * @param userId 사용자 ID
     * @param sha    커밋 SHA
     * @return 존재하면 true
     */
    boolean existsByUserIdAndSha(Long userId, String sha);

    /**
     * 사용자 ID, 저장소 이름, SHA로 커밋 문서 존재 여부를 확인한다.
     *
     * @param userId         사용자 ID
     * @param repositoryName 저장소 이름
     * @param sha            커밋 SHA
     * @return 존재하면 true
     */
    boolean existsByUserIdAndRepositoryNameAndSha(Long userId, String repositoryName, String sha);

    /**
     * 특정 사용자의 기간 내 커밋 수를 조회한다 (시즌 점수 배치용).
     *
     * @param userId 사용자 ID
     * @param start  시작 시각 (포함)
     * @param end    종료 시각 (포함)
     * @return 커밋 수
     */
    long countByUserIdAndAuthoredAtBetween(Long userId, Instant start, Instant end);

    /**
     * 특정 사용자의 기간 내 커밋 목록을 조회한다 (일별 cap 계산용).
     *
     * @param userId 사용자 ID
     * @param start  시작 시각 (포함)
     * @param end    종료 시각 (포함)
     * @return 커밋 문서 목록
     */
    List<CommitDocument> findByUserIdAndAuthoredAtBetween(Long userId, Instant start, Instant end);
}
