package io.swkoreatech.kosp.collection.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.swkoreatech.kosp.collection.document.ContributedRepoDocument;

/**
 * 기여 저장소 문서 MongoDB 리포지토리.
 *
 * <p>사용자가 기여한 GitHub 저장소 데이터에 대한 CRUD 및 조회 기능을 제공한다.
 */
public interface ContributedRepoDocumentRepository extends MongoRepository<ContributedRepoDocument, String> {

    /**
     * 사용자 ID로 기여 저장소 문서 목록을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 기여 저장소 문서 목록
     */
    List<ContributedRepoDocument> findByUserId(Long userId);

    /**
     * 사용자 ID와 전체 이름으로 기여 저장소 문서 존재 여부를 확인한다.
     *
     * @param userId   사용자 ID
     * @param fullName "owner/name" 형식의 저장소 전체 이름
     * @return 존재하면 true
     */
    boolean existsByUserIdAndFullName(Long userId, String fullName);
}
