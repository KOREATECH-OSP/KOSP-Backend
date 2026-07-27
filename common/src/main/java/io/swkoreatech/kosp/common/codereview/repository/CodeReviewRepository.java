package io.swkoreatech.kosp.common.codereview.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.swkoreatech.kosp.common.codereview.model.CodeReview;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;

public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {

    List<CodeReview> findByRepoOwnerAndRepositoryNameAndParentIdIsNullOrderByCreatedAtDesc(
        String repoOwner, String repositoryName);

    List<CodeReview> findByParentIdOrderByCreatedAtAsc(Long parentId);

    long countByRepoOwnerAndRepositoryNameAndParentIdIsNull(String repoOwner, String repositoryName);

    default CodeReview getById(Long id) {
        return findById(id).orElseThrow(() -> new GlobalException(ExceptionMessage.CODE_REVIEW_NOT_FOUND));
    }
}
