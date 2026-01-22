package io.swkoreatech.kosp.domain.challenge.repository;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.domain.challenge.model.Challenge;
import io.swkoreatech.kosp.domain.challenge.model.ChallengeHistory;
import io.swkoreatech.kosp.domain.user.model.User;

public interface ChallengeHistoryRepository extends Repository<ChallengeHistory, Long> {
    ChallengeHistory save(ChallengeHistory challengeHistory);
    boolean existsByUserAndChallenge(User user, Challenge challenge);
    java.util.List<ChallengeHistory> findAllByUserId(Long userId);
}
