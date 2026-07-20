package io.swkoreatech.kosp.common.organization.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.organization.model.Organization;
import io.swkoreatech.kosp.common.organization.model.OrganizationStatus;

public interface OrganizationRepository extends Repository<Organization, Long> {

    Organization save(Organization organization);

    Optional<Organization> findById(Long id);

    Optional<Organization> findByGithubOrgId(Long githubOrgId);

    List<Organization> findAllByRegisteredByUserId(Long registeredByUserId);

    List<Organization> findAll();

    List<Organization> findByGithubOrgNameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
        String githubOrgName, String displayName);

    boolean existsByGithubOrgId(Long githubOrgId);

    @Modifying
    @Query("UPDATE Organization o SET o.status = :status, o.registeredByUserId = :userId, o.githubOrgName = :orgName, o.displayName = :displayName, o.avatarUrl = :avatarUrl WHERE o.id = :id")
    void reactivate(
        @Param("id") Long id,
        @Param("status") OrganizationStatus status,
        @Param("userId") Long userId,
        @Param("orgName") String orgName,
        @Param("displayName") String displayName,
        @Param("avatarUrl") String avatarUrl
    );

    default Organization getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_NOT_FOUND));
    }

    default Organization getByGithubOrgId(Long githubOrgId) {
        return findByGithubOrgId(githubOrgId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_NOT_FOUND));
    }
}
