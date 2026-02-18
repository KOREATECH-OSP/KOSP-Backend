package io.swkoreatech.kosp.common.challenge.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.challenge.model.Challenge;
import io.swkoreatech.kosp.common.challenge.model.ChallengeHistory;
import io.swkoreatech.kosp.common.user.model.User;

public interface ChallengeHistoryRepository extends Repository<ChallengeHistory, Long> {
    ChallengeHistory save(ChallengeHistory challengeHistory);
    boolean existsByUserAndChallenge(User user, Challenge challenge);
    Optional<ChallengeHistory> findByUserAndChallenge(User user, Challenge challenge);
    java.util.List<ChallengeHistory> findAllByUserId(Long userId);
}
