package io.swkoreatech.kosp.domain.community.team.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamInvite;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.community.team.model.TeamRole;
import io.swkoreatech.kosp.domain.community.team.repository.TeamInviteRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubUserRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.common.IntegrationTestSupport;

@DisplayName("TeamInvite 통합 테스트")
class TeamInviteIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GithubUserRepository githubUserRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamInviteRepository teamInviteRepository;

    private User leader;
    private User invitee;
    private Team team;
    private TeamInvite invite;

    @BeforeEach
    void setUp() throws Exception {
        createGithubUser(3001L);
        createGithubUser(3002L);

        leader = User.builder()
            .name("팀장")
            .kutId("2024201")
            .kutEmail("invite-leader@koreatech.ac.kr")
            .password(passwordEncoder.encode(getValidPassword()))
            .roles(new HashSet<>())
            .build();
        leader = userRepository.save(leader);
        ReflectionTestUtils.setField(leader, "githubUser", githubUserRepository.getByGithubId(3001L));
        leader = userRepository.save(leader);

        invitee = User.builder()
            .name("피초대자")
            .kutId("2024202")
            .kutEmail("invitee@koreatech.ac.kr")
            .password(passwordEncoder.encode(getValidPassword()))
            .roles(new HashSet<>())
            .build();
        invitee = userRepository.save(invitee);
        ReflectionTestUtils.setField(invitee, "githubUser", githubUserRepository.getByGithubId(3002L));
        invitee = userRepository.save(invitee);

        team = Team.builder()
            .name("초대테스트팀")
            .description("팀 초대 통합 테스트")
            .build();
        team = teamRepository.save(team);

        TeamMember leaderMember = TeamMember.builder()
            .team(team)
            .user(leader)
            .role(TeamRole.LEADER)
            .build();
        teamMemberRepository.save(leaderMember);

        invite = TeamInvite.builder()
            .team(team)
            .inviter(leader)
            .invitee(invitee)
            .expiresAt(Instant.now().plus(3, ChronoUnit.DAYS))
            .build();
        invite = teamInviteRepository.save(invite);
    }

    @Nested
    @DisplayName("초대 조회 API")
    class GetInviteTest {

        @Test
        @DisplayName("초대 ID로 조회 성공")
        void getInvite_success() throws Exception {
            mockMvc.perform(get("/v1/teams/invites/{inviteId}", invite.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invite.getId()))
                .andExpect(jsonPath("$.team.id").value(team.getId()))
                .andExpect(jsonPath("$.team.name").value(team.getName()))
                .andExpect(jsonPath("$.team.memberCount").value(1))
                .andExpect(jsonPath("$.inviter.id").value(leader.getId()))
                .andExpect(jsonPath("$.inviter.name").value(leader.getName()))
                .andExpect(jsonPath("$.invitee.id").value(invitee.getId()))
                .andExpect(jsonPath("$.invitee.name").value(invitee.getName()))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 404")
        void getInvite_notFound() throws Exception {
            mockMvc.perform(get("/v1/teams/invites/{inviteId}", 99999L))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("삭제된 초대는 조회 불가")
        void getInvite_deleted() throws Exception {
            invite.delete();
            teamInviteRepository.save(invite);

            mockMvc.perform(get("/v1/teams/invites/{inviteId}", invite.getId()))
                .andExpect(status().isNotFound());
        }
    }
}
