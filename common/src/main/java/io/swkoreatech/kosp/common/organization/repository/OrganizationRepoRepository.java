package io.swkoreatech.kosp.common.organization.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.organization.model.OrganizationRepo;

public interface OrganizationRepoRepository extends Repository<OrganizationRepo, Long> {

    OrganizationRepo save(OrganizationRepo repo);

    List<OrganizationRepo> saveAll(Iterable<OrganizationRepo> repos);

    Optional<OrganizationRepo> findById(Long id);

    List<OrganizationRepo> findAllByOrganizationId(Long organizationId);

    @Modifying
    @Query("DELETE FROM OrganizationRepo r WHERE r.organization.id = :organizationId")
    void deleteAllByOrganizationId(@Param("organizationId") Long organizationId);

    default OrganizationRepo getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_REPO_NOT_FOUND));
    }
}
