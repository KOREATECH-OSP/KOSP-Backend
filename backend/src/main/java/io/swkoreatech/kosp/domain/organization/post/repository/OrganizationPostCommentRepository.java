package io.swkoreatech.kosp.domain.organization.post.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostComment;

public interface OrganizationPostCommentRepository extends JpaRepository<OrganizationPostComment, Long> {

    List<OrganizationPostComment> findAllByPostIdOrderByCreatedAtAsc(Long postId);

    Optional<OrganizationPostComment> findByIdAndPostId(Long id, Long postId);
}
