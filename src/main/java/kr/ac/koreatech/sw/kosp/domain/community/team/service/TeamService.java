package kr.ac.koreatech.sw.kosp.domain.community.team.service;

import java.time.Instant;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamInviteRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamDetailResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.Team;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.TeamInvite;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.TeamMember;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.TeamRole;
import kr.ac.koreatech.sw.kosp.domain.community.team.repository.TeamInviteRepository;
import kr.ac.koreatech.sw.kosp.domain.community.team.repository.TeamMemberRepository;
import kr.ac.koreatech.sw.kosp.domain.community.team.repository.TeamRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import kr.ac.koreatech.sw.kosp.infra.email.eventlistener.event.TeamInviteSendEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamInviteRepository teamInviteRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long create(User user, TeamCreateRequest request) {
        Team team = Team.builder()
            .name(request.name())
            .description(request.description())
            .imageUrl(request.imageUrl())
            .build();
        teamRepository.save(team);

        TeamMember leader = TeamMember.builder()
            .team(team)
            .user(user)
            .role(TeamRole.LEADER)
            .build();
        teamMemberRepository.save(leader);

        return team.getId();
    }

    public TeamDetailResponse getTeam(Long teamId) {
        Team team = teamRepository.getById(teamId);
        return TeamDetailResponse.from(team);
    }

    public TeamListResponse getList(String search, Pageable pageable) {
        Page<Team> page = teamRepository.findByNameContaining(search, pageable);
        List<TeamResponse> teams = page.getContent().stream()
            .map(team -> TeamResponse.from(team, getLeader(team)))
            .toList();
        return new TeamListResponse(teams, PageMeta.from(page));
    }

    private User getLeader(Team team) {
        return team.getMembers().stream()
            .filter(member -> member.getRole() == TeamRole.LEADER)
            .findFirst()
            .map(TeamMember::getUser)
            .orElse(null);
    }

    @Transactional
    public void update(Long teamId, User user, TeamUpdateRequest request) {
        Team team = teamRepository.getById(teamId);
        validateLeader(team, user);

        team.update(request.name(), request.description(), request.imageUrl());
    }

    @Transactional
    public void inviteMember(Long teamId, User user, TeamInviteRequest request, String clientUrl) {
        Team team = teamRepository.getById(teamId);
        validateLeader(team, user);

        User invitee = userRepository.getByKutEmail(request.email());
        if (teamMemberRepository.existsByTeamAndUser(team, invitee)) {
            throw new GlobalException(ExceptionMessage.TEAM_ALREADY_JOINED);
        }

        // Check/Delete existing invite
        teamInviteRepository.findByTeamAndInvitee(team, invitee)
            .ifPresent(teamInviteRepository::delete);

        TeamInvite invite = TeamInvite.builder()
            .team(team)
            .inviter(user)
            .invitee(invitee)
            .expiresAt(Instant.now().plus(3, java.time.temporal.ChronoUnit.DAYS))
            .build();
        teamInviteRepository.save(invite);

        eventPublisher.publishEvent(new TeamInviteSendEvent(
            invitee.getKutEmail(),
            team.getName(),
            user.getName(),
            invite.getId(),
            clientUrl
        ));
    }

    @Transactional
    public void acceptInvite(Long inviteId, User user) {
        TeamInvite invite = teamInviteRepository.findById(inviteId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND)); // Using standard 404

        if (!invite.getInvitee().getId().equals(user.getId())) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }

        if (invite.isExpired()) {
            teamInviteRepository.delete(invite);
            throw new GlobalException(ExceptionMessage.INVITATION_EXPIRED);
        }

        if (teamMemberRepository.existsByTeamAndUser(invite.getTeam(), user)) {
             teamInviteRepository.delete(invite);
             return; // Already joined
        }

        TeamMember member = TeamMember.builder()
            .team(invite.getTeam())
            .user(user)
            .role(TeamRole.MEMBER)
            .build();
        teamMemberRepository.save(member);
        
        teamInviteRepository.delete(invite);
    }

    @Transactional
    public void rejectInvite(Long inviteId, User user) {
        TeamInvite invite = teamInviteRepository.findById(inviteId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        if (!invite.getInvitee().getId().equals(user.getId())) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
        
        teamInviteRepository.delete(invite);
    }

    @Transactional
    public void removeMember(Long teamId, User user, Long targetUserId) {
        Team team = teamRepository.getById(teamId);
        validateLeader(team, user);

        if (user.getId().equals(targetUserId)) {
            throw new GlobalException(ExceptionMessage.LEADER_CANNOT_LEAVE);
        }

        User targetUser = userRepository.getById(targetUserId);
        TeamMember member = teamMemberRepository.findByTeamAndUser(team, targetUser)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        teamMemberRepository.delete(member);
    }

    private void validateLeader(Team team, User user) {
        TeamMember member = teamMemberRepository.findByTeamAndUser(team, user)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.FORBIDDEN));
        
        if (member.getRole() != TeamRole.LEADER) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }

    public List<TeamDetailResponse> getMyTeams(User user) {
        return teamMemberRepository.findAllByUser(user).stream()
            .map(member -> TeamDetailResponse.from(member.getTeam()))
            .toList();
    }
}
