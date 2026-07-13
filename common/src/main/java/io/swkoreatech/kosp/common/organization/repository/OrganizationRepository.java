package io.swkoreatech.kosp.common.organization.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.organization.model.Organization;

public interface OrganizationRepository extends Repository<Organization, Long> {

    Organization save(Organization organization);

    Optional<Organization> findById(Long id);

    Optional<Organization> findByGithubOrgId(Long githubOrgId);

    List<Organization> findAllByRegisteredByUserId(Long registeredByUserId);

    List<Organization> findAll();

    List<Organization> findByGithubOrgNameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
        String githubOrgName, String displayName);

    boolean existsByGithubOrgId(Long githubOrgId);

    default Organization getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_NOT_FOUND));
    }

    default Organization getByGithubOrgId(Long githubOrgId) {
        return findByGithubOrgId(githubOrgId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_NOT_FOUND));
    }
}
