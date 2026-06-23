package io.swkoreatech.kosp.common.organization.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.organization.model.OrganizationRepo;

public interface OrganizationRepoRepository extends Repository<OrganizationRepo, Long> {

    List<OrganizationRepo> saveAll(Iterable<OrganizationRepo> repos);

    List<OrganizationRepo> findAllByOrganizationId(Long organizationId);
}
