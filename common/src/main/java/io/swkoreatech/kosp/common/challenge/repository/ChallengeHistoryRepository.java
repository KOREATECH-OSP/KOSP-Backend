package io.swkoreatech.kosp.common.challenge.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.challenge.model.Challenge;
import io.swkoreatech.kosp.common.challenge.model.ChallengeHistory;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * {@link ChallengeHistory} 엔티티의 데이터 접근 리포지토리.
 *
 * <p>사용자별 챌린지 달성 이력의 저장 및 조회 기능을 제공한다.</p>
 */
public interface ChallengeHistoryRepository extends Repository<ChallengeHistory, Long> {

    /**
     * 챌린지 이력을 저장한다.
     *
     * @param challengeHistory 저장할 챌린지 이력
     * @return 저장된 챌린지 이력
     */
    ChallengeHistory save(ChallengeHistory challengeHistory);

    /**
     * 특정 사용자의 특정 챌린지 이력 존재 여부를 확인한다.
     *
     * @param user      사용자
     * @param challenge 챌린지
     * @return 이력이 존재하면 {@code true}
     */
    boolean existsByUserAndChallenge(User user, Challenge challenge);

    /**
     * 특정 사용자의 특정 챌린지 이력을 조회한다.
     *
     * @param user      사용자
     * @param challenge 챌린지
     * @return 챌린지 이력 (존재하지 않으면 빈 {@link Optional})
     */
    Optional<ChallengeHistory> findByUserAndChallenge(User user, Challenge challenge);

    /**
     * 특정 사용자의 모든 챌린지 이력을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 챌린지 이력 목록
     */
    java.util.List<ChallengeHistory> findAllByUserId(Long userId);

    /**
     * 특정 사용자가 달성한 챌린지 수를 조회한다.
     * 칭호 평가 배치에서 CHALLENGE_COUNT_GTE 조건 확인에 사용한다.
     *
     * @param user 사용자
     * @return 달성한 챌린지 수
     */
    long countByUserAndIsAchievedTrue(User user);
}
