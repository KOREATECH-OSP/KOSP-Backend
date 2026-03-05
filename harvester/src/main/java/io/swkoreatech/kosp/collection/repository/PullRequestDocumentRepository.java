package io.swkoreatech.kosp.collection.repository;

import io.swkoreatech.kosp.collection.document.PullRequestDocument;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 풀 리퀘스트 문서 MongoDB 리포지토리.
 *
 * <p>GitHub PR 데이터에 대한 CRUD 및 사용자/저장소별 조회 기능을 제공한다.
 */
public interface PullRequestDocumentRepository extends MongoRepository<PullRequestDocument, String> {

    /**
     * 사용자 ID로 PR 문서 목록을 조회한다.
     *
     * @param userId 사용자 ID
     * @return PR 문서 목록
     */
    List<PullRequestDocument> findByUserId(Long userId);

    /**
     * 사용자 ID와 저장소 이름으로 PR 문서 목록을 조회한다.
     *
     * @param userId         사용자 ID
     * @param repositoryName 저장소 이름
     * @return PR 문서 목록
     */
    List<PullRequestDocument> findByUserIdAndRepositoryName(Long userId, String repositoryName);

    /**
     * 사용자 ID와 PR 번호로 PR 문서 존재 여부를 확인한다.
     *
     * @param userId   사용자 ID
     * @param prNumber PR 번호
     * @return 존재하면 true
     */
    boolean existsByUserIdAndPrNumber(Long userId, Long prNumber);

    /**
     * 사용자 ID, 저장소 이름, PR 번호로 PR 문서 존재 여부를 확인한다.
     *
     * @param userId         사용자 ID
     * @param repositoryName 저장소 이름
     * @param prNumber       PR 번호
     * @return 존재하면 true
     */
    boolean existsByUserIdAndRepositoryNameAndPrNumber(Long userId, String repositoryName, Long prNumber);
}
