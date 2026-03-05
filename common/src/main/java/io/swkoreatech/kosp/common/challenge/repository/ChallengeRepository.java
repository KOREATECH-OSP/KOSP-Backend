package io.swkoreatech.kosp.common.challenge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.swkoreatech.kosp.common.challenge.model.Challenge;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;

/**
 * {@link Challenge} 엔티티의 데이터 접근 리포지토리.
 *
 * <p>챌린지 조회, 검색 및 동적 쿼리 기능을 제공한다.</p>
 */
public interface ChallengeRepository extends JpaRepository<Challenge, Long>, JpaSpecificationExecutor<Challenge> {

    /**
     * 특정 티어에 해당하는 챌린지 목록을 조회한다.
     *
     * @param tier 챌린지 티어
     * @return 해당 티어의 챌린지 목록
     */
    List<Challenge> findByTier(Integer tier);

    /**
     * 이름에 키워드를 포함하는 챌린지 목록을 조회한다.
     *
     * @param keyword 검색 키워드
     * @return 키워드를 포함하는 챌린지 목록
     */
    List<Challenge> findByNameContaining(String keyword);

    /**
     * ID로 챌린지를 조회하며, 존재하지 않으면 예외를 발생시킨다.
     *
     * @param id 챌린지 ID
     * @return 조회된 챌린지
     * @throws GlobalException 챌린지가 존재하지 않는 경우
     */
    default Challenge getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.CHALLENGE_NOT_FOUND));
    }
}
