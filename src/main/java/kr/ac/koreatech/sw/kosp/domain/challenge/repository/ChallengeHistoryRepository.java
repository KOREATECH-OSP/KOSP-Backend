package kr.ac.koreatech.sw.kosp.domain.challenge.repository;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.challenge.model.Challenge;
import kr.ac.koreatech.sw.kosp.domain.challenge.model.ChallengeHistory;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public interface ChallengeHistoryRepository extends Repository<ChallengeHistory, Long> {
    ChallengeHistory save(ChallengeHistory challengeHistory);
    boolean existsByUserAndChallenge(User user, Challenge challenge);
}
