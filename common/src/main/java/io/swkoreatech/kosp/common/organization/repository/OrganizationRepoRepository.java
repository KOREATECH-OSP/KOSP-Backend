package io.swkoreatech.kosp.common.organization.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.organization.model.OrganizationRepo;

public interface OrganizationRepoRepository extends Repository<OrganizationRepo, Long> {

    List<OrganizationRepo> saveAll(Iterable<OrganizationRepo> repos);

    List<OrganizationRepo> findAllByOrganizationId(Long organizationId);

    @Modifying
    @Query("DELETE FROM OrganizationRepo r WHERE r.organization.id = :organizationId")
    void deleteAllByOrganizationId(Long organizationId);
}
