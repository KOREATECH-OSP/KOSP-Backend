package io.swkoreatech.kosp.domain.community.team.service;

import java.time.Instant;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.community.team.dto.request.TeamCreateRequest;
import io.swkoreatech.kosp.domain.community.team.dto.request.TeamInviteRequest;
import io.swkoreatech.kosp.domain.community.team.dto.request.TeamUpdateRequest;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamDetailResponse;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamListResponse;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamResponse;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamInvite;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.community.team.model.TeamRole;
import io.swkoreatech.kosp.domain.community.team.repository.TeamInviteRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.notification.event.NotificationEvent;
import io.swkoreatech.kosp.domain.notification.model.NotificationType;
import io.swkoreatech.kosp.global.dto.PageMeta;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import io.swkoreatech.kosp.global.util.RsqlUtils;
import io.swkoreatech.kosp.infra.email.eventlistener.event.TeamInviteSendEvent;
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

    public TeamListResponse getList(String search, String rsql, Pageable pageable) {
        Specification<Team> spec = createSpecification(search, rsql);
        Page<Team> page = teamRepository.findAll(spec, pageable);
        List<TeamResponse> teams = page.getContent().stream()
            .map(team -> TeamResponse.from(team, getLeader(team)))
            .toList();
        return new TeamListResponse(teams, PageMeta.from(page));
    }

    private Specification<Team> createSpecification(String search, String rsql) {
        Specification<Team> searchSpec = (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            return builder.like(root.get("name"), "%" + search + "%");
        };
        return RsqlUtils.toSpecification(rsql, searchSpec);
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
    public void deleteTeam(Long teamId, User user) {
        Team team = teamRepository.getById(teamId);
        validateLeader(team, user);
        deleteAllTeamMembers(team);
        deleteAllTeamInvites(team);
        team.delete();
    }

    private void deleteAllTeamMembers(Team team) {
        List<TeamMember> members = teamMemberRepository.findAllByTeam(team);
        members.forEach(TeamMember::delete);
    }

    private void deleteAllTeamInvites(Team team) {
        List<TeamInvite> invites = teamInviteRepository.findAllByTeam(team);
        invites.forEach(TeamInvite::delete);
    }

    @Transactional
    public void inviteMember(Long teamId, User user, TeamInviteRequest request, String clientUrl) {
        Team team = teamRepository.getById(teamId);
        validateLeader(team, user);

         User invitee = userRepository.getByKutEmail(request.email());
         if (teamMemberRepository.existsByTeamAndUserAndIsDeletedFalse(team, invitee)) {
             throw new GlobalException(ExceptionMessage.TEAM_ALREADY_JOINED);
         }

         // Check/Delete existing invite
         teamInviteRepository.findByTeamAndInviteeAndIsDeletedFalse(team, invitee)
             .ifPresent(TeamInvite::delete);

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

        eventPublisher.publishEvent(NotificationEvent.of(
            invitee.getId(),
            NotificationType.TEAM_INVITED,
            team.getName() + " 팀에 초대되었습니다",
            user.getName() + "님이 " + team.getName() + " 팀에 초대했습니다",
            invite.getId()
        ));
    }

     @Transactional
     public void acceptInvite(Long inviteId, User user) {
         TeamInvite invite = teamInviteRepository.findByIdAndIsDeletedFalse(inviteId)
             .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND)); // Using standard 404

        if (!invite.getInvitee().getId().equals(user.getId())) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }

         if (invite.isExpired()) {
             invite.delete();
             throw new GlobalException(ExceptionMessage.INVITATION_EXPIRED);
         }

         if (teamMemberRepository.existsByTeamAndUserAndIsDeletedFalse(invite.getTeam(), user)) {
              invite.delete();
              return; // Already joined
         }

        TeamMember member = TeamMember.builder()
            .team(invite.getTeam())
            .user(user)
            .role(TeamRole.MEMBER)
            .build();
         teamMemberRepository.save(member);
         
         invite.delete();
    }

     @Transactional
     public void rejectInvite(Long inviteId, User user) {
         TeamInvite invite = teamInviteRepository.findByIdAndIsDeletedFalse(inviteId)
             .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

         if (!invite.getInvitee().getId().equals(user.getId())) {
             throw new GlobalException(ExceptionMessage.FORBIDDEN);
         }
         
         invite.delete();
    }

    @Transactional
    public void removeMember(Long teamId, User user, Long targetUserId) {
        Team team = teamRepository.getById(teamId);
        validateLeader(team, user);

        if (user.getId().equals(targetUserId)) {
            throw new GlobalException(ExceptionMessage.LEADER_CANNOT_LEAVE);
        }

         User targetUser = userRepository.getById(targetUserId);
         TeamMember member = teamMemberRepository.findByTeamAndUserAndIsDeletedFalse(team, targetUser)
             .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

         member.delete();
    }

     private void validateLeader(Team team, User user) {
         TeamMember member = teamMemberRepository.findByTeamAndUserAndIsDeletedFalse(team, user)
             .orElseThrow(() -> new GlobalException(ExceptionMessage.FORBIDDEN));
        
        if (member.getRole() != TeamRole.LEADER) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }

      public List<TeamDetailResponse> getMyTeams(User user) {
          return teamMemberRepository.findAllByUserAndIsDeletedFalse(user).stream()
              .map(member -> TeamDetailResponse.from(member.getTeam()))
              .toList();
      }

      public TeamDetailResponse getMyTeam(User user) {
          TeamMember member = teamMemberRepository.findByUser(user)
              .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
          return TeamDetailResponse.from(member.getTeam());
      }
}
