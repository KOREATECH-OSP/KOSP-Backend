package io.swkoreatech.kosp.domain.community.team.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@DisplayName("TeamController 통합 테스트")
class TeamControllerTest extends IntegrationTestSupport {

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
    private User member;
    private Team team;
    private String leaderToken;
    private String memberToken;

    @BeforeEach
    void setUp() throws Exception {
        createGithubUser(2001L);
        createGithubUser(2002L);
        createGithubUser(2003L);

        leader = User.builder()
            .name("팀장")
            .kutId("2024101")
            .kutEmail("leader@koreatech.ac.kr")
            .password(passwordEncoder.encode(getValidPassword()))
            .roles(new HashSet<>())
            .build();
        leader = userRepository.save(leader);
        ReflectionTestUtils.setField(leader, "githubUser", githubUserRepository.getByGithubId(2001L));
        leader = userRepository.save(leader);

        member = User.builder()
            .name("팀원")
            .kutId("2024102")
            .kutEmail("member@koreatech.ac.kr")
            .password(passwordEncoder.encode(getValidPassword()))
            .roles(new HashSet<>())
            .build();
        member = userRepository.save(member);
        ReflectionTestUtils.setField(member, "githubUser", githubUserRepository.getByGithubId(2002L));
        member = userRepository.save(member);

        team = Team.builder()
            .name("삭제테스트팀")
            .description("팀 삭제 통합 테스트")
            .build();
        team = teamRepository.save(team);

        TeamMember leaderMember = TeamMember.builder()
            .team(team)
            .user(leader)
            .role(TeamRole.LEADER)
            .build();
        teamMemberRepository.save(leaderMember);

        TeamMember regularMember = TeamMember.builder()
            .team(team)
            .user(member)
            .role(TeamRole.MEMBER)
            .build();
        teamMemberRepository.save(regularMember);

        leaderToken = loginAndGetToken("leader@koreatech.ac.kr", getValidPassword());
        memberToken = loginAndGetToken("member@koreatech.ac.kr", getValidPassword());
    }

    @Test
    @DisplayName("리더가 팀 삭제 → 200 OK")
    void deleteTeam_byLeader_returns200() throws Exception {
        mockMvc.perform(delete("/v1/teams/{teamId}", team.getId())
                .header("Authorization", "Bearer " + leaderToken))
            .andExpect(status().isOk());

        Team deletedTeam = teamRepository.findById(team.getId()).orElseThrow();
        assertThat(deletedTeam.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("일반 멤버가 팀 삭제 → 403 FORBIDDEN")
    void deleteTeam_byMember_returns403() throws Exception {
        mockMvc.perform(delete("/v1/teams/{teamId}", team.getId())
                .header("Authorization", "Bearer " + memberToken))
            .andExpect(status().isForbidden());

        Team notDeletedTeam = teamRepository.findById(team.getId()).orElseThrow();
        assertThat(notDeletedTeam.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("인증 안 된 사용자가 팀 삭제 → 401 UNAUTHORIZED")
    void deleteTeam_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/v1/teams/{teamId}", team.getId()))
            .andExpect(status().isUnauthorized());

        Team notDeletedTeam = teamRepository.findById(team.getId()).orElseThrow();
        assertThat(notDeletedTeam.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 팀 삭제 → 404 NOT_FOUND")
    void deleteTeam_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/v1/teams/{teamId}", 99999L)
                .header("Authorization", "Bearer " + leaderToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("팀 삭제 시 DB에서 team.isDeleted=true 확인")
    void deleteTeam_verifyTeamDeleted() throws Exception {
        mockMvc.perform(delete("/v1/teams/{teamId}", team.getId())
                .header("Authorization", "Bearer " + leaderToken))
            .andExpect(status().isOk());

        Team deletedTeam = teamRepository.findById(team.getId()).orElseThrow();
        assertThat(deletedTeam.isDeleted()).isTrue();
        assertThat(deletedTeam.getName()).isEqualTo("삭제테스트팀");
    }

    @Test
    @DisplayName("팀 삭제 시 모든 members + invites isDeleted=true 확인")
    void deleteTeam_cascades_toAllEntities() throws Exception {
        User invitee = User.builder()
            .name("초대받은사용자")
            .kutId("2024103")
            .kutEmail("invitee@koreatech.ac.kr")
            .password(passwordEncoder.encode(getValidPassword()))
            .roles(new HashSet<>())
            .build();
        invitee = userRepository.save(invitee);
        ReflectionTestUtils.setField(invitee, "githubUser", githubUserRepository.getByGithubId(2003L));
        invitee = userRepository.save(invitee);

        TeamInvite invite1 = TeamInvite.builder()
            .team(team)
            .invitee(invitee)
            .build();
        teamInviteRepository.save(invite1);

        TeamInvite invite2 = TeamInvite.builder()
            .team(team)
            .invitee(invitee)
            .build();
        teamInviteRepository.save(invite2);

        mockMvc.perform(delete("/v1/teams/{teamId}", team.getId())
                .header("Authorization", "Bearer " + leaderToken))
            .andExpect(status().isOk());

        Team deletedTeam = teamRepository.findById(team.getId()).orElseThrow();
        assertThat(deletedTeam.isDeleted()).isTrue();

        List<TeamMember> allMembers = teamMemberRepository.findAllByTeam(team);
        assertThat(allMembers).hasSize(2);
        assertThat(allMembers).allMatch(TeamMember::isDeleted);

        List<TeamInvite> allInvites = teamInviteRepository.findAllByTeam(team);
        assertThat(allInvites).hasSize(2);
        assertThat(allInvites).allMatch(TeamInvite::isDeleted);
    }

    @Test
    @DisplayName("rsql로 활성 팀만 필터링")
    void getList_withRsqlFilterActiveTeams_returnsOnlyActiveTeams() throws Exception {
        Team deletedTeam = Team.builder()
            .name("삭제된팀")
            .description("삭제된 팀")
            .build();
        ReflectionTestUtils.setField(deletedTeam, "isDeleted", true);
        teamRepository.save(deletedTeam);

        mockMvc.perform(get("/v1/teams")
                .param("rsql", "isDeleted==false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.teams[*].isDeleted").value(everyItem(is(false))));
    }

    @Test
    @DisplayName("rsql로 삭제된 팀만 필터링")
    void getList_withRsqlFilterDeletedTeams_returnsOnlyDeletedTeams() throws Exception {
        Team deletedTeam = Team.builder()
            .name("삭제된팀")
            .description("삭제된 팀")
            .build();
        ReflectionTestUtils.setField(deletedTeam, "isDeleted", true);
        teamRepository.save(deletedTeam);

        mockMvc.perform(get("/v1/teams")
                .param("rsql", "isDeleted==true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.teams[*].isDeleted").value(everyItem(is(true))));
    }

    @Test
    @DisplayName("검색과 rsql이 AND로 결합")
    void getList_withSearchAndRsql_combinesWithAnd() throws Exception {
        Team deletedTeam = Team.builder()
            .name("삭제테스트팀")
            .description("삭제된 팀")
            .build();
        ReflectionTestUtils.setField(deletedTeam, "isDeleted", true);
        teamRepository.save(deletedTeam);

        mockMvc.perform(get("/v1/teams")
                .param("search", "삭제테스트팀")
                .param("rsql", "isDeleted==false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.teams[*].isDeleted").value(everyItem(is(false))))
            .andExpect(jsonPath("$.teams[*].name").value(everyItem(is("삭제테스트팀"))));
    }

}
