package io.swkoreatech.kosp.domain.community.team.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.community.team.dto.request.TeamCreateRequest;
import io.swkoreatech.kosp.domain.community.team.dto.request.TeamUpdateRequest;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamDetailResponse;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamListResponse;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamInvite;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.community.team.model.TeamRole;
import io.swkoreatech.kosp.domain.community.team.repository.TeamInviteRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService 단위 테스트")
class TeamServiceTest {

    @InjectMocks
    private TeamService teamService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private TeamInviteRepository teamInviteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User createUser(Long id, String name) {
        User user = User.builder()
            .name(name)
            .kutId("2024" + id)
            .kutEmail(name + "@koreatech.ac.kr")
            .password("password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Team createTeam(Long id, String name) {
        Team team = Team.builder()
            .name(name)
            .description(name + " 팀")
            .build();
        ReflectionTestUtils.setField(team, "id", id);
        ReflectionTestUtils.setField(team, "members", new ArrayList<>());
        return team;
    }

    private TeamMember createTeamMember(Long id, Team team, User user, TeamRole role) {
        TeamMember member = TeamMember.builder()
            .team(team)
            .user(user)
            .role(role)
            .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    @Nested
    @DisplayName("create 메서드")
    class CreateTest {

        @Test
        @DisplayName("팀을 생성하고 생성자를 리더로 등록한다")
        void createsTeamWithLeader() {
            // given
            User leader = createUser(1L, "리더");
            TeamCreateRequest request = new TeamCreateRequest("테스트팀", "팀 설명", null);
            
            doAnswer(invocation -> {
                Team team = invocation.getArgument(0);
                ReflectionTestUtils.setField(team, "id", 1L);
                return team;
            }).when(teamRepository).save(any(Team.class));

            // when
            Long teamId = teamService.create(leader, request);

            // then
            assertThat(teamId).isEqualTo(1L);
            verify(teamRepository).save(any(Team.class));
            verify(teamMemberRepository).save(any(TeamMember.class));
        }
    }

    @Nested
    @DisplayName("getTeam 메서드")
    class GetTeamTest {

        @Test
        @DisplayName("팀 상세 정보를 조회한다")
        void returnsTeamDetail() {
            // given
            Team team = createTeam(1L, "테스트팀");
            given(teamRepository.getById(1L)).willReturn(team);

            // when
            TeamDetailResponse response = teamService.getTeam(1L);

            // then
            assertThat(response.name()).isEqualTo("테스트팀");
        }
    }

    @Nested
    @DisplayName("getList 메서드")
    class GetListTest {

        @Test
        @DisplayName("팀 목록을 검색어로 필터링하여 조회한다")
        void returnsFilteredTeamList() {
            // given
            User leader = createUser(1L, "리더");
            Team team = createTeam(1L, "테스트팀");
            TeamMember leaderMember = createTeamMember(1L, team, leader, TeamRole.LEADER);
            team.getMembers().add(leaderMember);
            
            Pageable pageable = PageRequest.of(0, 10);
            Page<Team> page = new PageImpl<>(List.of(team), pageable, 1);
            
            given(teamRepository.findByNameContaining("테스트", pageable)).willReturn(page);

            // when
            TeamListResponse result = teamService.getList("테스트", pageable);

            // then
            assertThat(result.teams()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update 메서드")
    class UpdateTest {

        @Test
        @DisplayName("리더가 아니면 수정 시 예외가 발생한다")
        void throwsException_whenNotLeader() {
            // given
            User leader = createUser(1L, "리더");
            User member = createUser(2L, "멤버");
            Team team = createTeam(1L, "테스트팀");
            TeamMember memberRole = createTeamMember(1L, team, member, TeamRole.MEMBER);
            TeamUpdateRequest request = new TeamUpdateRequest("수정된 이름", "수정된 설명", null);
            
            given(teamRepository.getById(1L)).willReturn(team);
            given(teamMemberRepository.findByTeamAndUser(team, member)).willReturn(Optional.of(memberRole));

            // when & then
            assertThatThrownBy(() -> teamService.update(1L, member, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("리더가 팀 정보를 수정한다")
        void updatesTeam_whenLeader() {
            // given
            User leader = createUser(1L, "리더");
            Team team = createTeam(1L, "기존팀");
            TeamMember leaderMember = createTeamMember(1L, team, leader, TeamRole.LEADER);
            TeamUpdateRequest request = new TeamUpdateRequest("수정된 이름", "수정된 설명", null);
            
            given(teamRepository.getById(1L)).willReturn(team);
            given(teamMemberRepository.findByTeamAndUser(team, leader)).willReturn(Optional.of(leaderMember));

            // when
            teamService.update(1L, leader, request);

            // then
            assertThat(team.getName()).isEqualTo("수정된 이름");
            assertThat(team.getDescription()).isEqualTo("수정된 설명");
        }
    }

    @Nested
    @DisplayName("acceptInvite 메서드")
    class AcceptInviteTest {

        @Test
        @DisplayName("초대 대상이 아니면 예외가 발생한다")
        void throwsException_whenNotInvitee() {
            // given
            User invitee = createUser(1L, "초대받은자");
            User other = createUser(2L, "다른 사용자");
            Team team = createTeam(1L, "테스트팀");
            TeamInvite invite = TeamInvite.builder()
                .team(team)
                .invitee(invitee)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
            ReflectionTestUtils.setField(invite, "id", 1L);
            
            given(teamInviteRepository.findById(1L)).willReturn(Optional.of(invite));

            // when & then
            assertThatThrownBy(() -> teamService.acceptInvite(1L, other))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("만료된 초대장은 예외가 발생한다")
        void throwsException_whenExpired() {
            // given
            User invitee = createUser(1L, "초대받은자");
            Team team = createTeam(1L, "테스트팀");
            TeamInvite invite = TeamInvite.builder()
                .team(team)
                .invitee(invitee)
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();
            ReflectionTestUtils.setField(invite, "id", 1L);
            
            given(teamInviteRepository.findById(1L)).willReturn(Optional.of(invite));

            // when & then
            assertThatThrownBy(() -> teamService.acceptInvite(1L, invitee))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("초대를 수락하면 팀 멤버로 등록된다")
        void addsTeamMember_whenAccepted() {
            // given
            User invitee = createUser(1L, "초대받은자");
            Team team = createTeam(1L, "테스트팀");
            TeamInvite invite = TeamInvite.builder()
                .team(team)
                .invitee(invitee)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
            ReflectionTestUtils.setField(invite, "id", 1L);
            
            given(teamInviteRepository.findById(1L)).willReturn(Optional.of(invite));
            given(teamMemberRepository.existsByTeamAndUser(team, invitee)).willReturn(false);

            // when
            teamService.acceptInvite(1L, invitee);

            // then
            verify(teamMemberRepository).save(any(TeamMember.class));
            verify(teamInviteRepository).delete(invite);
        }
    }

    @Nested
    @DisplayName("removeMember 메서드")
    class RemoveMemberTest {

        @Test
        @DisplayName("리더는 자기 자신을 제거할 수 없다")
        void throwsException_whenLeaderTriesToRemoveSelf() {
            // given
            User leader = createUser(1L, "리더");
            Team team = createTeam(1L, "테스트팀");
            TeamMember leaderMember = createTeamMember(1L, team, leader, TeamRole.LEADER);
            
            given(teamRepository.getById(1L)).willReturn(team);
            given(teamMemberRepository.findByTeamAndUser(team, leader)).willReturn(Optional.of(leaderMember));

            // when & then
            assertThatThrownBy(() -> teamService.removeMember(1L, leader, 1L))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("리더가 다른 멤버를 제거한다")
        void removesMember_whenLeader() {
            // given
            User leader = createUser(1L, "리더");
            User member = createUser(2L, "멤버");
            Team team = createTeam(1L, "테스트팀");
            TeamMember leaderMember = createTeamMember(1L, team, leader, TeamRole.LEADER);
            TeamMember memberRole = createTeamMember(2L, team, member, TeamRole.MEMBER);
            
            given(teamRepository.getById(1L)).willReturn(team);
            given(teamMemberRepository.findByTeamAndUser(team, leader)).willReturn(Optional.of(leaderMember));
            given(userRepository.getById(2L)).willReturn(member);
            given(teamMemberRepository.findByTeamAndUser(team, member)).willReturn(Optional.of(memberRole));

            // when
            teamService.removeMember(1L, leader, 2L);

            // then
            verify(teamMemberRepository).delete(memberRole);
        }
    }

    @Nested
    @DisplayName("rejectInvite 메서드")
    class RejectInviteTest {

        @Test
        @DisplayName("초대를 거절하면 초대가 삭제된다")
        void deletesInvite() {
            // given
            User invitee = createUser(1L, "초대받은자");
            Team team = createTeam(1L, "테스트팀");
            TeamInvite invite = TeamInvite.builder()
                .team(team)
                .invitee(invitee)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
            ReflectionTestUtils.setField(invite, "id", 1L);
            
            given(teamInviteRepository.findById(1L)).willReturn(Optional.of(invite));

            // when
            teamService.rejectInvite(1L, invitee);

            // then
            verify(teamInviteRepository).delete(invite);
        }

        @Test
        @DisplayName("초대 대상이 아니면 예외가 발생한다")
        void throwsException_whenNotInvitee() {
            // given
            User invitee = createUser(1L, "초대받은자");
            User other = createUser(2L, "다른 사용자");
            Team team = createTeam(1L, "테스트팀");
            TeamInvite invite = TeamInvite.builder()
                .team(team)
                .invitee(invitee)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
            ReflectionTestUtils.setField(invite, "id", 1L);
            
            given(teamInviteRepository.findById(1L)).willReturn(Optional.of(invite));

            // when & then
            assertThatThrownBy(() -> teamService.rejectInvite(1L, other))
                .isInstanceOf(GlobalException.class);
        }
    }

    @Nested
    @DisplayName("getMyTeams 메서드")
    class GetMyTeamsTest {

        @Test
        @DisplayName("소속된 팀이 없으면 빈 목록을 반환한다")
        void returnsEmptyList_whenNoTeams() {
            // given
            User user = createUser(1L, "사용자");
            given(teamMemberRepository.findByUser(user)).willReturn(Optional.empty());

            // when
            List<TeamDetailResponse> result = teamService.getMyTeams(user);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("소속된 팀 목록을 반환한다")
        void returnsTeamList() {
            // given
            User user = createUser(1L, "사용자");
            Team team = createTeam(1L, "내 팀");
            TeamMember member = createTeamMember(1L, team, user, TeamRole.MEMBER);
            
            given(teamMemberRepository.findByUser(user)).willReturn(Optional.of(member));

            // when
            List<TeamDetailResponse> result = teamService.getMyTeams(user);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("내 팀");
        }
    }

    @Nested
    @DisplayName("getMyTeam 메서드")
    class GetMyTeamTest {

        @Test
        @DisplayName("소속된 팀이 없으면 예외가 발생한다")
        void throwsException_whenNoTeam() {
            // given
            User user = createUser(1L, "사용자");
            given(teamMemberRepository.findByUser(user)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> teamService.getMyTeam(user))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("소속된 팀을 반환한다")
        void returnsTeam() {
            // given
            User user = createUser(1L, "사용자");
            Team team = createTeam(1L, "내 팀");
            TeamMember member = createTeamMember(1L, team, user, TeamRole.MEMBER);
            
            given(teamMemberRepository.findByUser(user)).willReturn(Optional.of(member));

            // when
            TeamDetailResponse result = teamService.getMyTeam(user);

            // then
            assertThat(result.name()).isEqualTo("내 팀");
        }
    }

     @Nested
     @DisplayName("acceptInvite - already joined 메서드")
     class AcceptInviteAlreadyJoinedTest {

         @Test
         @DisplayName("이미 팀에 가입된 경우 초대만 삭제된다")
         void deletesInvite_whenAlreadyJoined() {
             // given
             User invitee = createUser(1L, "초대받은자");
             Team team = createTeam(1L, "테스트팀");
             TeamInvite invite = TeamInvite.builder()
                 .team(team)
                 .invitee(invitee)
                 .expiresAt(Instant.now().plusSeconds(3600))
                 .build();
             ReflectionTestUtils.setField(invite, "id", 1L);
             
             given(teamInviteRepository.findById(1L)).willReturn(Optional.of(invite));
             given(teamMemberRepository.existsByTeamAndUser(team, invitee)).willReturn(true);

             // when
             teamService.acceptInvite(1L, invitee);

             // then
             verify(teamInviteRepository).delete(invite);
         }
     }

     @Nested
     @DisplayName("deleteTeam 메서드")
     class DeleteTeamTest {

         @Test
         @DisplayName("리더가 팀 삭제 → 성공")
         void deleteTeam_byLeader_success() {
             // given
             User leader = createUser(1L, "리더");
             Team team = createTeam(1L, "테스트팀");
             TeamMember leaderMember = createTeamMember(1L, team, leader, TeamRole.LEADER);
             
             given(teamRepository.getById(1L)).willReturn(team);
             given(teamMemberRepository.findByTeamAndUser(team, leader)).willReturn(Optional.of(leaderMember));
             given(teamMemberRepository.findAllByTeam(team)).willReturn(List.of(leaderMember));
             given(teamInviteRepository.findAllByTeam(team)).willReturn(List.of());

             // when
             teamService.deleteTeam(1L, leader);

             // then
             verify(team).delete();
             verify(leaderMember).delete();
         }

         @Test
         @DisplayName("일반 멤버가 팀 삭제 → FORBIDDEN")
         void deleteTeam_byMember_throwsForbidden() {
             // given
             User leader = createUser(1L, "리더");
             User member = createUser(2L, "멤버");
             Team team = createTeam(1L, "테스트팀");
             TeamMember memberRole = createTeamMember(2L, team, member, TeamRole.MEMBER);
             
             given(teamRepository.getById(1L)).willReturn(team);
             given(teamMemberRepository.findByTeamAndUser(team, member)).willReturn(Optional.of(memberRole));

             // when & then
             assertThatThrownBy(() -> teamService.deleteTeam(1L, member))
                 .isInstanceOf(GlobalException.class);
         }

         @Test
         @DisplayName("팀 삭제 시 모든 TeamMember + TeamInvite soft delete")
         void deleteTeam_cascadesSoftDelete_toMembersAndInvites() {
             // given
             User leader = createUser(1L, "리더");
             User member2 = createUser(2L, "멤버2");
             User member3 = createUser(3L, "멤버3");
             User invitee = createUser(4L, "초대받은자");
             
             Team team = createTeam(1L, "테스트팀");
             TeamMember leaderMember = createTeamMember(1L, team, leader, TeamRole.LEADER);
             TeamMember member2Role = createTeamMember(2L, team, member2, TeamRole.MEMBER);
             TeamMember member3Role = createTeamMember(3L, team, member3, TeamRole.MEMBER);
             
             TeamInvite invite1 = TeamInvite.builder()
                 .team(team)
                 .invitee(invitee)
                 .expiresAt(Instant.now().plusSeconds(3600))
                 .build();
             ReflectionTestUtils.setField(invite1, "id", 1L);
             
             TeamInvite invite2 = TeamInvite.builder()
                 .team(team)
                 .invitee(invitee)
                 .expiresAt(Instant.now().plusSeconds(3600))
                 .build();
             ReflectionTestUtils.setField(invite2, "id", 2L);
             
             given(teamRepository.getById(1L)).willReturn(team);
             given(teamMemberRepository.findByTeamAndUser(team, leader)).willReturn(Optional.of(leaderMember));
             given(teamMemberRepository.findAllByTeam(team)).willReturn(List.of(leaderMember, member2Role, member3Role));
             given(teamInviteRepository.findAllByTeam(team)).willReturn(List.of(invite1, invite2));

             // when
             teamService.deleteTeam(1L, leader);

             // then
             verify(team).delete();
             verify(leaderMember).delete();
             verify(member2Role).delete();
             verify(member3Role).delete();
             verify(invite1).delete();
             verify(invite2).delete();
         }

         @Test
         @DisplayName("삭제된 팀은 목록에 미노출")
         void deletedTeam_notInList() {
             // given
             User leader = createUser(1L, "리더");
             Team team = createTeam(1L, "테스트팀");
             TeamMember leaderMember = createTeamMember(1L, team, leader, TeamRole.LEADER);
             
             given(teamRepository.getById(1L)).willReturn(team);
             given(teamMemberRepository.findByTeamAndUser(team, leader)).willReturn(Optional.of(leaderMember));
             given(teamMemberRepository.findAllByTeam(team)).willReturn(List.of(leaderMember));
             given(teamInviteRepository.findAllByTeam(team)).willReturn(List.of());
             
             Pageable pageable = PageRequest.of(0, 10);
             Page<Team> emptyPage = new PageImpl<>(List.of(), pageable, 0);
             given(teamRepository.findByNameContaining("테스트", pageable)).willReturn(emptyPage);

             // when
             teamService.deleteTeam(1L, leader);
             TeamListResponse result = teamService.getList("테스트", pageable);

             // then
             verify(team).delete();
             assertThat(result.teams()).isEmpty();
         }
     }
}
