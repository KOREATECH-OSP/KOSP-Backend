package io.swkoreatech.kosp.domain.organization.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.organization.model.Organization;
import io.swkoreatech.kosp.common.organization.model.OrganizationStatus;
import io.swkoreatech.kosp.common.organization.repository.OrganizationRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.organization.model.OrganizationBookmark;
import io.swkoreatech.kosp.domain.organization.repository.OrganizationBookmarkRepository;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationBookmarkService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationBookmarkRepository organizationBookmarkRepository;

    @Transactional
    public boolean toggleBookmark(Long organizationId, User user) {
        Organization organization = organizationRepository.getById(organizationId);
        if (organization.getStatus() == OrganizationStatus.DISCONNECTED) {
            throw new GlobalException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }
        return organizationBookmarkRepository.findByUserAndOrganization(user, organization)
            .map(existing -> {
                organizationBookmarkRepository.delete(existing);
                return false;
            })
            .orElseGet(() -> {
                organizationBookmarkRepository.save(
                    OrganizationBookmark.builder().user(user).organization(organization).build()
                );
                return true;
            });
    }

    public List<OrganizationResponse> getBookmarkedOrganizations(User user) {
        return organizationBookmarkRepository.findOrganizationsByUserId(user.getId()).stream()
            .filter(org -> org.getStatus() != OrganizationStatus.DISCONNECTED)
            .map(OrganizationResponse::from)
            .toList();
    }

    public boolean isBookmarked(Long organizationId, User user) {
        Organization organization = organizationRepository.getById(organizationId);
        return organizationBookmarkRepository.existsByUserAndOrganization(user, organization);
    }
}
