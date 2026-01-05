package kr.ac.koreatech.sw.kosp.domain.challenge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.koreatech.sw.kosp.domain.challenge.model.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByTier(Integer tier);
    
    List<Challenge> findByNameContaining(String keyword);
}
