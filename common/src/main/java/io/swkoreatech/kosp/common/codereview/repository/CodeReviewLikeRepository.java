package io.swkoreatech.kosp.common.codereview.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.common.codereview.model.CodeReviewLike;

public interface CodeReviewLikeRepository extends JpaRepository<CodeReviewLike, Long> {

    Optional<CodeReviewLike> findByUserIdAndCodeReviewId(Long userId, Long codeReviewId);

    @Query("SELECT l.codeReview.id FROM CodeReviewLike l WHERE l.user.id = :userId AND l.codeReview.id IN :reviewIds")
    Set<Long> findLikedReviewIds(@Param("userId") Long userId, @Param("reviewIds") List<Long> reviewIds);
}
