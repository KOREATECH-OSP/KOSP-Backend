package io.swkoreatech.kosp.domain.community.recruit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitApplyDecisionRequest;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply.ApplyStatus;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitApplyRepository;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitRepository;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.community.team.model.TeamRole;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.domain.user.model.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecruitApplyService 단위 테스트")
class RecruitApplyServiceTest {

    @InjectMocks
    private RecruitApplyService recruitApplyService;

    @Mock
    private RecruitRepository recruitRepository;

    @Mock
    private RecruitApplyRepository recruitApplyRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    private User createUser(Long id, String name) {
        User user = mock(User.class);
        given(user.getId()).willReturn(id);
        given(user.getName()).willReturn(name);
        return user;
    }

    private Team createTeam(Long id, String name) {
        Team team = mock(Team.class);
        given(team.getId()).willReturn(id);
        given(team.getName()).willReturn(name);
        return team;
    }

    private Recruit createRecruit(Long id, Team team) {
        Recruit recruit = mock(Recruit.class);
        given(recruit.getId()).willReturn(id);
        given(recruit.getTeam()).willReturn(team);
        return recruit;
    }

    private RecruitApply createRecruitApply(Long id, Recruit recruit, User user) {
        RecruitApply apply = mock(RecruitApply.class);
        given(apply.getId()).willReturn(id);
        given(apply.getRecruit()).willReturn(recruit);
        given(apply.getUser()).willReturn(user);
        given(apply.getStatus()).willReturn(ApplyStatus.PENDING);
        return apply;
    }

    private TeamMember createTeamMember(Team team, User user, TeamRole role) {
        TeamMember member = mock(TeamMember.class);
        given(member.getTeam()).willReturn(team);
        given(member.getUser()).willReturn(user);
        given(member.getRole()).willReturn(role);
        return member;
    }

    @Nested
    @DisplayName("decideApplication 메서드")
    class DecideApplicationTest {

        @Test
        @DisplayName("사유 입력 시 성공")
        void decideApplication_사유입력_성공() {
            // given
            User leader = createUser(1L, "팀장");
            User applicant = createUser(2L, "지원자");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, team);
            RecruitApply apply = createRecruitApply(1L, recruit, applicant);
            TeamMember leaderMember = createTeamMember(team, leader, TeamRole.LEADER);
            
            String reason = "지원자의 역량이 우수하여 수락합니다.";
            RecruitApplyDecisionRequest request = new RecruitApplyDecisionRequest(
                ApplyStatus.ACCEPTED, 
                reason
            );

            given(recruitApplyRepository.findById(1L)).willReturn(Optional.of(apply));
            given(teamMemberRepository.findByTeamAndUser(team, leader)).willReturn(Optional.of(leaderMember));
            given(teamMemberRepository.existsByTeamAndUser(team, applicant)).willReturn(false);

            // when
            recruitApplyService.decideApplication(1L, leader, request);

            // then
            verify(apply).updateDecisionReason(reason);
            verify(recruitApplyRepository).save(apply);
        }

        @Test
        @DisplayName("사유 null 시 기본값 저장")
        void decideApplication_사유null_기본값저장() {
            // given
            User leader = createUser(1L, "팀장");
            User applicant = createUser(2L, "지원자");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, team);
            RecruitApply apply = createRecruitApply(1L, recruit, applicant);
            TeamMember leaderMember = createTeamMember(team, leader, TeamRole.LEADER);
            
            RecruitApplyDecisionRequest request = new RecruitApplyDecisionRequest(
                ApplyStatus.REJECTED, 
                null
            );

            given(recruitApplyRepository.findById(1L)).willReturn(Optional.of(apply));
            given(teamMemberRepository.findByTeamAndUser(team, leader)).willReturn(Optional.of(leaderMember));

            // when
            recruitApplyService.decideApplication(1L, leader, request);

            // then
            verify(apply).updateDecisionReason(null);
            verify(recruitApplyRepository).save(apply);
        }

        @Test
        @DisplayName("사유 공백 시 기본값 저장")
        void decideApplication_사유공백_기본값저장() {
            // given
            User leader = createUser(1L, "팀장");
            User applicant = createUser(2L, "지원자");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, team);
            RecruitApply apply = createRecruitApply(1L, recruit, applicant);
            TeamMember leaderMember = createTeamMember(team, leader, TeamRole.LEADER);
            
            RecruitApplyDecisionRequest request = new RecruitApplyDecisionRequest(
                ApplyStatus.REJECTED, 
                "   "
            );

            given(recruitApplyRepository.findById(1L)).willReturn(Optional.of(apply));
            given(teamMemberRepository.findByTeamAndUser(team, leader)).willReturn(Optional.of(leaderMember));

            // when
            recruitApplyService.decideApplication(1L, leader, request);

            // then
            verify(apply).updateDecisionReason("   ");
            verify(recruitApplyRepository).save(apply);
        }

        @Test
        @DisplayName("수락 시 사유 입력")
        void decideApplication_수락_사유입력() {
            // given
            User leader = createUser(1L, "팀장");
            User applicant = createUser(2L, "지원자");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, team);
            RecruitApply apply = createRecruitApply(1L, recruit, applicant);
            TeamMember leaderMember = createTeamMember(team, leader, TeamRole.LEADER);
            
            String acceptReason = "프로젝트 경험이 풍부하여 팀에 도움이 될 것으로 판단됩니다.";
            RecruitApplyDecisionRequest request = new RecruitApplyDecisionRequest(
                ApplyStatus.ACCEPTED, 
                acceptReason
            );

            given(recruitApplyRepository.findById(1L)).willReturn(Optional.of(apply));
            given(teamMemberRepository.findByTeamAndUser(team, leader)).willReturn(Optional.of(leaderMember));
            given(teamMemberRepository.existsByTeamAndUser(team, applicant)).willReturn(false);

            // when
            recruitApplyService.decideApplication(1L, leader, request);

            // then
            verify(apply).updateStatus(ApplyStatus.ACCEPTED);
            verify(apply).updateDecisionReason(acceptReason);
            verify(teamMemberRepository).save(any(TeamMember.class));
            verify(recruitApplyRepository).save(apply);
        }

        @Test
        @DisplayName("거절 시 사유 입력")
        void decideApplication_거절_사유입력() {
            // given
            User leader = createUser(1L, "팀장");
            User applicant = createUser(2L, "지원자");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, team);
            RecruitApply apply = createRecruitApply(1L, recruit, applicant);
            TeamMember leaderMember = createTeamMember(team, leader, TeamRole.LEADER);
            
            String rejectReason = "현재 팀 인원이 충분하여 추가 모집을 하지 않습니다.";
            RecruitApplyDecisionRequest request = new RecruitApplyDecisionRequest(
                ApplyStatus.REJECTED, 
                rejectReason
            );

            given(recruitApplyRepository.findById(1L)).willReturn(Optional.of(apply));
            given(teamMemberRepository.findByTeamAndUser(team, leader)).willReturn(Optional.of(leaderMember));

            // when
            recruitApplyService.decideApplication(1L, leader, request);

            // then
            verify(apply).updateStatus(ApplyStatus.REJECTED);
            verify(apply).updateDecisionReason(rejectReason);
            verify(recruitApplyRepository).save(apply);
        }
    }
}
