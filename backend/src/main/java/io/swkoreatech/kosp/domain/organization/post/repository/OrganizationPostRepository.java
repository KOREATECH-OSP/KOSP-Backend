package io.swkoreatech.kosp.domain.organization.post.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPost;
import io.swkoreatech.kosp.domain.organization.post.model.OrganizationPostCategory;

public interface OrganizationPostRepository extends JpaRepository<OrganizationPost, Long> {

    @Query("SELECT p FROM OrganizationPost p WHERE p.organization.id = :orgId ORDER BY p.category DESC, p.createdAt DESC")
    List<OrganizationPost> findAllByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT p FROM OrganizationPost p WHERE p.organization.id = :orgId AND p.category = :category ORDER BY p.createdAt DESC")
    List<OrganizationPost> findAllByOrganizationIdAndCategory(@Param("orgId") Long orgId, @Param("category") OrganizationPostCategory category);

    Optional<OrganizationPost> findByIdAndOrganizationId(Long id, Long organizationId);
}
