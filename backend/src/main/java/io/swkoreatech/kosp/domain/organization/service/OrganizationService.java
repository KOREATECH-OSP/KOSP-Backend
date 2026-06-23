package io.swkoreatech.kosp.domain.organization.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.organization.model.Organization;
import io.swkoreatech.kosp.common.organization.model.OrganizationMember;
import io.swkoreatech.kosp.common.organization.model.OrganizationMemberRole;
import io.swkoreatech.kosp.common.organization.model.OrganizationMemberStatus;
import io.swkoreatech.kosp.common.organization.model.OrganizationRepo;
import io.swkoreatech.kosp.common.organization.repository.OrganizationMemberRepository;
import io.swkoreatech.kosp.common.organization.repository.OrganizationRepoRepository;
import io.swkoreatech.kosp.common.organization.repository.OrganizationRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.organization.dto.response.AvailableOrganizationResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationDetailResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationResponse;
import io.swkoreatech.kosp.infra.github.GithubOrgApiClient;
import io.swkoreatech.kosp.infra.github.dto.GithubOrgMember;
import io.swkoreatech.kosp.infra.github.dto.GithubOrgMembership;
import io.swkoreatech.kosp.infra.github.dto.GithubOrgRepo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationRepoRepository organizationRepoRepository;
    private final UserRepository userRepository;
    private final GithubOrgApiClient githubOrgApiClient;
    private final TextEncryptor textEncryptor;

    public List<AvailableOrganizationResponse> getAvailableOrganizations(User user) {
        String token = decryptToken(user);
        List<GithubOrgMembership> memberships = githubOrgApiClient.getMyOrgMemberships(token);
        Set<Long> registeredOrgIds = findRegisteredOrgIds(memberships);
        return memberships.stream()
            .filter(m -> "admin".equals(m.role()))
            .map(m -> AvailableOrganizationResponse.from(m, registeredOrgIds.contains(m.organization().id())))
            .toList();
    }

    @Transactional
    public OrganizationResponse registerOrganization(User user, Long githubOrgId) {
        validateNotRegistered(githubOrgId);
        String token = decryptToken(user);
        GithubOrgMembership membership = findOwnerMembership(token, githubOrgId);
        Organization organization = saveOrganization(membership, user.getId());
        syncMembers(organization, token);
        syncRepositories(organization, token);
        return OrganizationResponse.from(organization);
    }

    public List<OrganizationResponse> getMyOrganizations(User user) {
        return organizationRepository.findAllByRegisteredByUserId(user.getId()).stream()
            .map(OrganizationResponse::from)
            .toList();
    }

    public OrganizationDetailResponse getDetail(Long organizationId) {
        Organization organization = organizationRepository.getById(organizationId);
        List<OrganizationMember> members = organizationMemberRepository.findAllByOrganizationId(organizationId);
        int linkedCount = countLinked(members);
        int repoCount = organizationRepoRepository.findAllByOrganizationId(organizationId).size();
        return OrganizationDetailResponse.from(organization, members.size(), linkedCount, repoCount);
    }

    private String decryptToken(User user) {
        if (user.getGithubUser() == null) {
            throw new GlobalException(ExceptionMessage.GITHUB_USER_NOT_FOUND);
        }
        return textEncryptor.decrypt(user.getGithubUser().getGithubToken());
    }

    private Set<Long> findRegisteredOrgIds(List<GithubOrgMembership> memberships) {
        List<Long> orgIds = memberships.stream()
            .map(m -> m.organization().id())
            .toList();
        return orgIds.stream()
            .filter(organizationRepository::existsByGithubOrgId)
            .collect(Collectors.toSet());
    }

    private void validateNotRegistered(Long githubOrgId) {
        if (organizationRepository.existsByGithubOrgId(githubOrgId)) {
            throw new GlobalException(ExceptionMessage.ORGANIZATION_ALREADY_REGISTERED);
        }
    }

    private GithubOrgMembership findOwnerMembership(String token, Long githubOrgId) {
        return githubOrgApiClient.getMyOrgMemberships(token).stream()
            .filter(m -> githubOrgId.equals(m.organization().id()) && "admin".equals(m.role()))
            .findFirst()
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_OWNER_REQUIRED));
    }

    private Organization saveOrganization(GithubOrgMembership membership, Long registeredByUserId) {
        Organization organization = Organization.builder()
            .githubOrgId(membership.organization().id())
            .githubOrgName(membership.organization().login())
            .displayName(membership.organization().login())
            .avatarUrl(membership.organization().avatarUrl())
            .registeredByUserId(registeredByUserId)
            .build();
        return organizationRepository.save(organization);
    }

    private void syncMembers(Organization organization, String token) {
        List<GithubOrgMember> githubMembers = githubOrgApiClient.getOrgMembers(token, organization.getGithubOrgName());
        List<Long> githubIds = githubMembers.stream().map(GithubOrgMember::id).toList();
        Map<Long, Long> githubIdToUserId = buildGithubIdToUserIdMap(githubIds);
        LocalDateTime syncedAt = LocalDateTime.now();
        List<OrganizationMember> members = githubMembers.stream()
            .map(m -> buildMember(organization, m, githubIdToUserId.get(m.id()), syncedAt))
            .toList();
        organizationMemberRepository.saveAll(members);
    }

    private Map<Long, Long> buildGithubIdToUserIdMap(List<Long> githubIds) {
        return userRepository.findAllByGithubIds(githubIds).stream()
            .collect(Collectors.toMap(
                u -> u.getGithubUser().getGithubId(),
                User::getId
            ));
    }

    private OrganizationMember buildMember(
        Organization organization,
        GithubOrgMember githubMember,
        Long userId,
        LocalDateTime syncedAt
    ) {
        return OrganizationMember.builder()
            .organization(organization)
            .userId(userId)
            .githubUserId(githubMember.id())
            .githubUsername(githubMember.login())
            .role(OrganizationMemberRole.MEMBER)
            .syncedAt(syncedAt)
            .build();
    }

    private void syncRepositories(Organization organization, String token) {
        List<GithubOrgRepo> githubRepos = githubOrgApiClient.getOrgRepos(token, organization.getGithubOrgName());
        LocalDateTime syncedAt = LocalDateTime.now();
        List<OrganizationRepo> repos = githubRepos.stream()
            .map(r -> buildRepo(organization, r, syncedAt))
            .toList();
        organizationRepoRepository.saveAll(repos);
    }

    private OrganizationRepo buildRepo(Organization organization, GithubOrgRepo githubRepo, LocalDateTime syncedAt) {
        return OrganizationRepo.builder()
            .organization(organization)
            .githubRepoId(githubRepo.id())
            .repositoryName(githubRepo.name())
            .repositoryFullName(githubRepo.fullName())
            .repositoryUrl(githubRepo.htmlUrl())
            .visibility(githubRepo.visibility())
            .syncedAt(syncedAt)
            .build();
    }

    private int countLinked(List<OrganizationMember> members) {
        return (int) members.stream()
            .filter(m -> m.getStatus() == OrganizationMemberStatus.LINKED)
            .count();
    }
}
