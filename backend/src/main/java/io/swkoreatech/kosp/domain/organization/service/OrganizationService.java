package io.swkoreatech.kosp.domain.organization.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
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
import io.swkoreatech.kosp.common.organization.model.OrganizationStatus;
import io.swkoreatech.kosp.domain.organization.event.OrgRegisteredCollectionEvent;
import io.swkoreatech.kosp.infra.email.eventlistener.event.OrganizationRegisteredEvent;
import io.swkoreatech.kosp.common.organization.repository.OrganizationMemberRepository;
import io.swkoreatech.kosp.common.organization.repository.OrganizationRepoRepository;
import io.swkoreatech.kosp.common.organization.repository.OrganizationRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.organization.dto.response.AvailableOrganizationResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationDetailResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationMemberResponse;
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
    private final ApplicationEventPublisher eventPublisher;

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
    public OrganizationResponse registerOrganization(User user, Long githubOrgId, String clientUrl) {
        String token = decryptToken(user);
        GithubOrgMembership membership = findOwnerMembership(token, githubOrgId);
        Organization organization = findOrCreateOrganization(membership, user.getId());
        organizationMemberRepository.deleteAllByOrganizationId(organization.getId());
        organizationRepoRepository.deleteAllByOrganizationId(organization.getId());
        syncMembers(organization, token, user, clientUrl);
        syncRepositories(organization, token);
        eventPublisher.publishEvent(new OrgRegisteredCollectionEvent(
            this,
            organization.getId(),
            organization.getGithubOrgName(),
            organization.getGithubOrgId(),
            user.getId()
        ));
        return OrganizationResponse.from(organization);
    }

    public List<OrganizationResponse> getMyOrganizations(User user) {
        return organizationRepository.findAllByRegisteredByUserId(user.getId()).stream()
            .filter(org -> org.getStatus() != OrganizationStatus.DISCONNECTED)
            .map(OrganizationResponse::from)
            .toList();
    }

    public List<OrganizationMemberResponse> getMembers(Long organizationId, User user) {
        Organization organization = organizationRepository.getById(organizationId);
        if (organization.getStatus() == OrganizationStatus.DISCONNECTED) {
            throw new GlobalException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }
        if (!organization.getRegisteredByUserId().equals(user.getId())) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
        return organizationMemberRepository.findAllByOrganizationId(organizationId).stream()
            .filter(m -> m.getStatus() != OrganizationMemberStatus.REMOVED)
            .map(OrganizationMemberResponse::from)
            .toList();
    }

    public OrganizationDetailResponse getDetail(Long organizationId) {
        Organization organization = organizationRepository.getById(organizationId);
        if (organization.getStatus() == OrganizationStatus.DISCONNECTED) {
            throw new GlobalException(ExceptionMessage.ORGANIZATION_NOT_FOUND);
        }
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
            .filter(id -> organizationRepository.findByGithubOrgId(id)
                .filter(org -> org.getStatus() != OrganizationStatus.DISCONNECTED)
                .isPresent())
            .collect(Collectors.toSet());
    }

    private GithubOrgMembership findOwnerMembership(String token, Long githubOrgId) {
        return githubOrgApiClient.getMyOrgMemberships(token).stream()
            .filter(m -> githubOrgId.equals(m.organization().id()) && "admin".equals(m.role()))
            .findFirst()
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ORGANIZATION_OWNER_REQUIRED));
    }

    private Organization findOrCreateOrganization(GithubOrgMembership membership, Long registeredByUserId) {
        return organizationRepository.findByGithubOrgId(membership.organization().id())
            .map(existing -> {
                if (existing.getStatus() != OrganizationStatus.DISCONNECTED) {
                    throw new GlobalException(ExceptionMessage.ORGANIZATION_ALREADY_REGISTERED);
                }
                existing.activate();
                return organizationRepository.save(existing);
            })
            .orElseGet(() -> {
                Organization organization = Organization.builder()
                    .githubOrgId(membership.organization().id())
                    .githubOrgName(membership.organization().login())
                    .displayName(membership.organization().login())
                    .avatarUrl(membership.organization().avatarUrl())
                    .registeredByUserId(registeredByUserId)
                    .build();
                return organizationRepository.save(organization);
            });
    }

    private void syncMembers(Organization organization, String token, User owner, String clientUrl) {
        List<GithubOrgMember> githubMembers = githubOrgApiClient.getOrgMembers(token, organization.getGithubOrgName());
        List<Long> githubIds = githubMembers.stream().map(GithubOrgMember::id).toList();
        Map<Long, Long> githubIdToUserId = buildGithubIdToUserIdMap(githubIds);
        LocalDateTime syncedAt = LocalDateTime.now();
        List<OrganizationMember> members = githubMembers.stream()
            .map(m -> buildMemberWithEmailNotification(organization, m, githubIdToUserId.get(m.id()), syncedAt, token, owner, clientUrl))
            .toList();
        organizationMemberRepository.saveAll(members);
    }

    private OrganizationMember buildMemberWithEmailNotification(
        Organization organization,
        GithubOrgMember githubMember,
        Long userId,
        LocalDateTime syncedAt,
        String token,
        User owner,
        String clientUrl
    ) {
        if (userId != null) {
            return buildMember(organization, githubMember, userId, null, syncedAt);
        }
        String email = githubOrgApiClient.getUserEmail(token, githubMember.login());
        if (email != null) {
            eventPublisher.publishEvent(new OrganizationRegisteredEvent(
                email,
                githubMember.login(),
                owner.getName(),
                organization.getDisplayName(),
                clientUrl
            ));
            return buildMember(organization, githubMember, null, OrganizationMemberStatus.EMAIL_PENDING, syncedAt);
        }
        return buildMember(organization, githubMember, null, OrganizationMemberStatus.EMAIL_PRIVATE, syncedAt);
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
        OrganizationMemberStatus status,
        LocalDateTime syncedAt
    ) {
        return OrganizationMember.builder()
            .organization(organization)
            .userId(userId)
            .githubUserId(githubMember.id())
            .githubUsername(githubMember.login())
            .role(OrganizationMemberRole.MEMBER)
            .status(status)
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
