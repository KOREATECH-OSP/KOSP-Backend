package kr.ac.koreatech.sw.kosp.domain.challenge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import kr.ac.koreatech.sw.kosp.domain.challenge.model.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, Long>, JpaSpecificationExecutor<Challenge> {
    List<Challenge> findByTier(Integer tier);
    
    List<Challenge> findByNameContaining(String keyword);
}
