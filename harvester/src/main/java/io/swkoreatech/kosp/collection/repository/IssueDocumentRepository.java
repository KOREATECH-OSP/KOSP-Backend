package io.swkoreatech.kosp.collection.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.swkoreatech.kosp.collection.document.IssueDocument;

/**
 * 이슈 문서 MongoDB 리포지토리.
 *
 * <p>GitHub 이슈 데이터에 대한 CRUD 및 사용자/저장소별 조회 기능을 제공한다.
 */
public interface IssueDocumentRepository extends MongoRepository<IssueDocument, String> {

    /**
     * 사용자 ID로 이슈 문서 목록을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 이슈 문서 목록
     */
    List<IssueDocument> findByUserId(Long userId);

    /**
     * 사용자 ID와 저장소 이름으로 이슈 문서 목록을 조회한다.
     *
     * @param userId         사용자 ID
     * @param repositoryName 저장소 이름
     * @return 이슈 문서 목록
     */
    List<IssueDocument> findByUserIdAndRepositoryName(Long userId, String repositoryName);

    /**
     * 사용자 ID와 이슈 번호로 이슈 문서 존재 여부를 확인한다.
     *
     * @param userId      사용자 ID
     * @param issueNumber 이슈 번호
     * @return 존재하면 true
     */
    boolean existsByUserIdAndIssueNumber(Long userId, Long issueNumber);

    /**
     * 사용자 ID, 저장소 이름, 이슈 번호로 이슈 문서 존재 여부를 확인한다.
     *
     * @param userId         사용자 ID
     * @param repositoryName 저장소 이름
     * @param issueNumber    이슈 번호
     * @return 존재하면 true
     */
    boolean existsByUserIdAndRepositoryNameAndIssueNumber(Long userId, String repositoryName, Long issueNumber);
}
