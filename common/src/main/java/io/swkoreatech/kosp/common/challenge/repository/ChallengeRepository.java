package io.swkoreatech.kosp.common.challenge.repository;

import io.swkoreatech.kosp.common.challenge.model.Challenge;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChallengeRepository extends JpaRepository<Challenge, Long>, JpaSpecificationExecutor<Challenge> {

    List<Challenge> findByTier(Integer tier);
    
    List<Challenge> findByNameContaining(String keyword);

    default Challenge getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.CHALLENGE_NOT_FOUND));
    }
}
