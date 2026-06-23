package io.swkoreatech.kosp.domain.admin.organization.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import io.swkoreatech.kosp.domain.admin.organization.dto.response.AdminOrganizationMemberResponse;
import io.swkoreatech.kosp.domain.admin.organization.dto.response.AdminOrganizationRepoResponse;
import io.swkoreatech.kosp.domain.organization.dto.response.OrganizationResponse;
import io.swkoreatech.kosp.infra.github.GithubOrgApiClient;
import io.swkoreatech.kosp.infra.github.dto.GithubOrgMember;
import io.swkoreatech.kosp.infra.github.dto.GithubOrgRepo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationRepoRepository organizationRepoRepository;
    private final UserRepository userRepository;
    private final GithubOrgApiClient githubOrgApiClient;
    private final TextEncryptor textEncryptor;

    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
            .map(OrganizationResponse::from)
            .toList();
    }

    public List<AdminOrganizationMemberResponse> getMembers(Long organizationId) {
        return organizationMemberRepository.findAllByOrganizationId(organizationId).stream()
            .map(AdminOrganizationMemberResponse::from)
            .toList();
    }

    public List<AdminOrganizationRepoResponse> getRepositories(Long organizationId) {
        return organizationRepoRepository.findAllByOrganizationId(organizationId).stream()
            .map(AdminOrganizationRepoResponse::from)
            .toList();
    }

    @Transactional
    public void sync(Long organizationId) {
        Organization organization = organizationRepository.getById(organizationId);
        String token = resolveToken(organization);
        resyncMembers(organization, token);
        resyncRepositories(organization, token);
    }

    @Transactional
    public void deactivate(Long organizationId) {
        organizationRepository.getById(organizationId).disconnect();
    }

    private String resolveToken(Organization organization) {
        User registeredBy = userRepository.getById(organization.getRegisteredByUserId());
        return textEncryptor.decrypt(registeredBy.getGithubUser().getGithubToken());
    }

    private void resyncMembers(Organization organization, String token) {
        List<GithubOrgMember> githubMembers = githubOrgApiClient.getOrgMembers(token, organization.getGithubOrgName());
        Set<Long> currentGithubIds = toGithubIdSet(githubMembers);
        List<OrganizationMember> existing = organizationMemberRepository.findAllByOrganizationId(organization.getId());
        markRemovedMembers(existing, currentGithubIds);
        saveNewMembers(organization, githubMembers, existing);
    }

    private Set<Long> toGithubIdSet(List<GithubOrgMember> members) {
        return members.stream().map(GithubOrgMember::id).collect(Collectors.toSet());
    }

    private void markRemovedMembers(List<OrganizationMember> existing, Set<Long> currentGithubIds) {
        existing.stream()
            .filter(m -> !currentGithubIds.contains(m.getGithubUserId()))
            .filter(m -> m.getStatus() != OrganizationMemberStatus.REMOVED)
            .forEach(OrganizationMember::remove);
    }

    private void saveNewMembers(Organization organization, List<GithubOrgMember> githubMembers, List<OrganizationMember> existing) {
        Set<Long> existingIds = existing.stream().map(OrganizationMember::getGithubUserId).collect(Collectors.toSet());
        List<Long> newIds = githubMembers.stream().map(GithubOrgMember::id).filter(id -> !existingIds.contains(id)).toList();
        Map<Long, Long> githubIdToUserId = buildGithubIdToUserIdMap(newIds);
        LocalDateTime syncedAt = LocalDateTime.now();
        List<OrganizationMember> newMembers = githubMembers.stream()
            .filter(m -> !existingIds.contains(m.id()))
            .map(m -> buildMember(organization, m, githubIdToUserId.get(m.id()), syncedAt))
            .toList();
        organizationMemberRepository.saveAll(newMembers);
    }

    private Map<Long, Long> buildGithubIdToUserIdMap(List<Long> githubIds) {
        return userRepository.findAllByGithubIds(githubIds).stream()
            .collect(Collectors.toMap(u -> u.getGithubUser().getGithubId(), User::getId));
    }

    private OrganizationMember buildMember(Organization org, GithubOrgMember m, Long userId, LocalDateTime syncedAt) {
        return OrganizationMember.builder()
            .organization(org)
            .userId(userId)
            .githubUserId(m.id())
            .githubUsername(m.login())
            .role(OrganizationMemberRole.MEMBER)
            .syncedAt(syncedAt)
            .build();
    }

    private void resyncRepositories(Organization organization, String token) {
        List<GithubOrgRepo> githubRepos = githubOrgApiClient.getOrgRepos(token, organization.getGithubOrgName());
        List<OrganizationRepo> existing = organizationRepoRepository.findAllByOrganizationId(organization.getId());
        Set<Long> currentRepoIds = githubRepos.stream().map(GithubOrgRepo::id).collect(Collectors.toSet());
        existing.stream()
            .filter(r -> !currentRepoIds.contains(r.getGithubRepoId()))
            .filter(OrganizationRepo::isActive)
            .forEach(OrganizationRepo::deactivate);
        Set<Long> existingRepoIds = existing.stream().map(OrganizationRepo::getGithubRepoId).collect(Collectors.toSet());
        LocalDateTime syncedAt = LocalDateTime.now();
        List<OrganizationRepo> newRepos = githubRepos.stream()
            .filter(r -> !existingRepoIds.contains(r.id()))
            .map(r -> buildRepo(organization, r, syncedAt))
            .toList();
        organizationRepoRepository.saveAll(newRepos);
    }

    private OrganizationRepo buildRepo(Organization org, GithubOrgRepo githubRepo, LocalDateTime syncedAt) {
        return OrganizationRepo.builder()
            .organization(org)
            .githubRepoId(githubRepo.id())
            .repositoryName(githubRepo.name())
            .repositoryFullName(githubRepo.fullName())
            .repositoryUrl(githubRepo.htmlUrl())
            .visibility(githubRepo.visibility())
            .syncedAt(syncedAt)
            .build();
    }
}
