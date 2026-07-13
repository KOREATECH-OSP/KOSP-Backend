package io.swkoreatech.kosp.common.organization.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.organization.model.OrganizationMember;

public interface OrganizationMemberRepository extends Repository<OrganizationMember, Long> {

    OrganizationMember save(OrganizationMember member);

    List<OrganizationMember> saveAll(Iterable<OrganizationMember> members);

    Optional<OrganizationMember> findById(Long id);

    List<OrganizationMember> findAllByOrganizationId(Long organizationId);

    Optional<OrganizationMember> findByOrganizationIdAndGithubUserId(Long organizationId, Long githubUserId);

    List<OrganizationMember> findAllByGithubUserId(Long githubUserId);

    void deleteAllByOrganizationId(Long organizationId);

    default OrganizationMember getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_MEMBER_NOT_FOUND));
    }
}
