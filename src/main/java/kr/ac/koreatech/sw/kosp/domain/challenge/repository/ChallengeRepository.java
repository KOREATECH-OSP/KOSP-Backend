package kr.ac.koreatech.sw.kosp.domain.challenge.repository;

import kr.ac.koreatech.sw.kosp.domain.challenge.model.Challenge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface ChallengeRepository extends Repository<Challenge, Long> {
    List<Challenge> findAll();
    Challenge save(Challenge challenge);
    Optional<Challenge> findById(Long id);
    void delete(Challenge challenge);
    List<Challenge> findByNameContaining(String name);
}
