package io.swkoreatech.kosp.domain.organization.service;

import static io.swkoreatech.kosp.global.common.fixture.TestUserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.organization.model.Organization;
import io.swkoreatech.kosp.common.organization.model.OrganizationMember;
import io.swkoreatech.kosp.common.organization.model.OrganizationMemberStatus;
import io.swkoreatech.kosp.common.organization.repository.OrganizationMemberRepository;
import io.swkoreatech.kosp.common.organization.repository.OrganizationRepoRepository;
import io.swkoreatech.kosp.common.organization.repository.OrganizationRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.infra.email.eventlistener.event.OrganizationRegisteredEvent;
import io.swkoreatech.kosp.infra.github.GithubOrgApiClient;
import io.swkoreatech.kosp.infra.github.dto.GithubOrgMember;
import io.swkoreatech.kosp.infra.github.dto.GithubOrgMembership;
import io.swkoreatech.kosp.infra.github.dto.GithubOrgSummary;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationService 단위 테스트")
class OrganizationServiceTest {

    @InjectMocks
    private OrganizationService organizationService;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private OrganizationRepoRepository organizationRepoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GithubOrgApiClient githubOrgApiClient;

    @Mock
    private TextEncryptor textEncryptor;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private static final String CLIENT_URL = "https://kosp.koreatech.ac.kr";
    private static final Long GITHUB_ORG_ID = 100L;
    private static final String ORG_NAME = "koreatech-osp";

    private User createOwner() {
        User owner = createUser(1L, "홍길동");
        GithubUser githubUser = GithubUser.builder()
            .githubId(10L)
            .githubLogin("gildong-hong")
            .githubToken("encrypted-token")
            .build();
        ReflectionTestUtils.setField(owner, "githubUser", githubUser);
        return owner;
    }

    private Organization createOrganization() {
        Organization org = Organization.builder()
            .githubOrgId(GITHUB_ORG_ID)
            .githubOrgName(ORG_NAME)
            .displayName(ORG_NAME)
            .registeredByUserId(1L)
            .build();
        ReflectionTestUtils.setField(org, "id", 1L);
        return org;
    }

    private GithubOrgMembership createOwnerMembership() {
        GithubOrgSummary orgSummary = new GithubOrgSummary(GITHUB_ORG_ID, ORG_NAME, null);
        return new GithubOrgMembership("admin", "active", orgSummary);
    }

    @Nested
    @DisplayName("registerOrganization - 멤버 상태 분기")
    class RegisterOrganizationTest {

        @Test
        @DisplayName("이미 K-OSP에 가입된 멤버는 LINKED 상태로 저장된다")
        void linkedMember_whenAlreadyJoined() {
            // given
            User owner = createOwner();
            Organization org = createOrganization();
            GithubOrgMember githubMember = new GithubOrgMember(20L, "member-login", null);

            given(organizationRepository.existsByGithubOrgId(GITHUB_ORG_ID)).willReturn(false);
            given(textEncryptor.decrypt("encrypted-token")).willReturn("plain-token");
            given(githubOrgApiClient.getMyOrgMemberships("plain-token")).willReturn(List.of(createOwnerMembership()));
            given(organizationRepository.save(any())).willReturn(org);
            given(githubOrgApiClient.getOrgMembers("plain-token", ORG_NAME)).willReturn(List.of(githubMember));
            // githubId 20L → userId 5L 로 매핑 (K-OSP 가입자)
            User existingUser = createUser(5L, "기존멤버");
            GithubUser existingGithubUser = GithubUser.builder().githubId(20L).build();
            ReflectionTestUtils.setField(existingUser, "githubUser", existingGithubUser);
            given(userRepository.findAllByGithubIds(List.of(20L))).willReturn(List.of(existingUser));
            given(githubOrgApiClient.getOrgRepos("plain-token", ORG_NAME)).willReturn(List.of());

            // when
            organizationService.registerOrganization(owner, GITHUB_ORG_ID, CLIENT_URL);

            // then
            ArgumentCaptor<List<OrganizationMember>> captor = ArgumentCaptor.forClass(List.class);
            verify(organizationMemberRepository).saveAll(captor.capture());

            OrganizationMember saved = captor.getValue().get(0);
            assertThat(saved.getStatus()).isEqualTo(OrganizationMemberStatus.LINKED);
            assertThat(saved.getUserId()).isEqualTo(5L);
            verify(eventPublisher, never()).publishEvent(any(OrganizationRegisteredEvent.class));
        }

        @Test
        @DisplayName("미가입 멤버 중 GitHub 이메일이 공개된 경우 EMAIL_PENDING 상태로 저장되고 이벤트가 발행된다")
        void emailPending_whenEmailPublic() {
            // given
            User owner = createOwner();
            Organization org = createOrganization();
            GithubOrgMember githubMember = new GithubOrgMember(30L, "new-member", null);

            given(organizationRepository.existsByGithubOrgId(GITHUB_ORG_ID)).willReturn(false);
            given(textEncryptor.decrypt("encrypted-token")).willReturn("plain-token");
            given(githubOrgApiClient.getMyOrgMemberships("plain-token")).willReturn(List.of(createOwnerMembership()));
            given(organizationRepository.save(any())).willReturn(org);
            given(githubOrgApiClient.getOrgMembers("plain-token", ORG_NAME)).willReturn(List.of(githubMember));
            given(userRepository.findAllByGithubIds(List.of(30L))).willReturn(List.of()); // 미가입자
            given(githubOrgApiClient.getUserEmail("plain-token", "new-member")).willReturn("new-member@example.com");
            given(githubOrgApiClient.getOrgRepos("plain-token", ORG_NAME)).willReturn(List.of());

            // when
            organizationService.registerOrganization(owner, GITHUB_ORG_ID, CLIENT_URL);

            // then
            ArgumentCaptor<List<OrganizationMember>> memberCaptor = ArgumentCaptor.forClass(List.class);
            verify(organizationMemberRepository).saveAll(memberCaptor.capture());

            OrganizationMember saved = memberCaptor.getValue().get(0);
            assertThat(saved.getStatus()).isEqualTo(OrganizationMemberStatus.EMAIL_PENDING);
            assertThat(saved.getUserId()).isNull();

            ArgumentCaptor<OrganizationRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(OrganizationRegisteredEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            OrganizationRegisteredEvent event = eventCaptor.getValue();
            assertThat(event.email()).isEqualTo("new-member@example.com");
            assertThat(event.githubUsername()).isEqualTo("new-member");
            assertThat(event.ownerName()).isEqualTo("홍길동");
            assertThat(event.orgName()).isEqualTo(ORG_NAME);
            assertThat(event.clientUrl()).isEqualTo(CLIENT_URL);
        }

        @Test
        @DisplayName("미가입 멤버 중 GitHub 이메일이 비공개인 경우 EMAIL_PRIVATE 상태로 저장되고 이벤트가 발행되지 않는다")
        void emailPrivate_whenEmailNull() {
            // given
            User owner = createOwner();
            Organization org = createOrganization();
            GithubOrgMember githubMember = new GithubOrgMember(40L, "private-member", null);

            given(organizationRepository.existsByGithubOrgId(GITHUB_ORG_ID)).willReturn(false);
            given(textEncryptor.decrypt("encrypted-token")).willReturn("plain-token");
            given(githubOrgApiClient.getMyOrgMemberships("plain-token")).willReturn(List.of(createOwnerMembership()));
            given(organizationRepository.save(any())).willReturn(org);
            given(githubOrgApiClient.getOrgMembers("plain-token", ORG_NAME)).willReturn(List.of(githubMember));
            given(userRepository.findAllByGithubIds(List.of(40L))).willReturn(List.of()); // 미가입자
            given(githubOrgApiClient.getUserEmail("plain-token", "private-member")).willReturn(null); // 이메일 비공개
            given(githubOrgApiClient.getOrgRepos("plain-token", ORG_NAME)).willReturn(List.of());

            // when
            organizationService.registerOrganization(owner, GITHUB_ORG_ID, CLIENT_URL);

            // then
            ArgumentCaptor<List<OrganizationMember>> captor = ArgumentCaptor.forClass(List.class);
            verify(organizationMemberRepository).saveAll(captor.capture());

            OrganizationMember saved = captor.getValue().get(0);
            assertThat(saved.getStatus()).isEqualTo(OrganizationMemberStatus.EMAIL_PRIVATE);
            verify(eventPublisher, never()).publishEvent(any(OrganizationRegisteredEvent.class));
        }

        @Test
        @DisplayName("혼합 멤버 시나리오 — LINKED 1명, EMAIL_PENDING 1명, EMAIL_PRIVATE 1명이 올바르게 저장된다")
        void mixed_members_savedWithCorrectStatus() {
            // given
            User owner = createOwner();
            Organization org = createOrganization();

            GithubOrgMember linkedMember = new GithubOrgMember(20L, "linked-member", null);
            GithubOrgMember pendingMember = new GithubOrgMember(30L, "pending-member", null);
            GithubOrgMember privateMember = new GithubOrgMember(40L, "private-member", null);

            User existingUser = createUser(5L, "기존멤버");
            GithubUser existingGithubUser = GithubUser.builder().githubId(20L).build();
            ReflectionTestUtils.setField(existingUser, "githubUser", existingGithubUser);

            given(organizationRepository.existsByGithubOrgId(GITHUB_ORG_ID)).willReturn(false);
            given(textEncryptor.decrypt("encrypted-token")).willReturn("plain-token");
            given(githubOrgApiClient.getMyOrgMemberships("plain-token")).willReturn(List.of(createOwnerMembership()));
            given(organizationRepository.save(any())).willReturn(org);
            given(githubOrgApiClient.getOrgMembers("plain-token", ORG_NAME))
                .willReturn(List.of(linkedMember, pendingMember, privateMember));
            given(userRepository.findAllByGithubIds(anyList())).willReturn(List.of(existingUser));
            given(githubOrgApiClient.getUserEmail("plain-token", "pending-member")).willReturn("pending@example.com");
            given(githubOrgApiClient.getUserEmail("plain-token", "private-member")).willReturn(null);
            given(githubOrgApiClient.getOrgRepos("plain-token", ORG_NAME)).willReturn(List.of());

            // when
            organizationService.registerOrganization(owner, GITHUB_ORG_ID, CLIENT_URL);

            // then
            ArgumentCaptor<List<OrganizationMember>> captor = ArgumentCaptor.forClass(List.class);
            verify(organizationMemberRepository).saveAll(captor.capture());

            List<OrganizationMember> saved = captor.getValue();
            assertThat(saved).hasSize(3);
            assertThat(saved).extracting(OrganizationMember::getStatus)
                .containsExactlyInAnyOrder(
                    OrganizationMemberStatus.LINKED,
                    OrganizationMemberStatus.EMAIL_PENDING,
                    OrganizationMemberStatus.EMAIL_PRIVATE
                );

            // 이메일 공개 멤버 1명에게만 이벤트 발행
            verify(eventPublisher, times(1)).publishEvent(any(OrganizationRegisteredEvent.class));
        }
    }
}
