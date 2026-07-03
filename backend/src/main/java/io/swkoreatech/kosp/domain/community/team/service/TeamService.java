package io.swkoreatech.kosp.domain.community.team.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.community.team.dto.request.TeamCreateRequest;
import io.swkoreatech.kosp.domain.community.team.dto.request.TeamInviteRequest;
import io.swkoreatech.kosp.domain.community.team.dto.request.TeamUpdateRequest;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamDetailResponse;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamListResponse;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamResponse;
import io.swkoreatech.kosp.domain.community.team.model.PendingTeamInvite;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamInvite;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.community.team.model.TeamRole;
import io.swkoreatech.kosp.domain.community.team.repository.PendingTeamInviteRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamInviteRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamRepository;
import io.swkoreatech.kosp.domain.notification.event.NotificationEvent;
import io.swkoreatech.kosp.domain.notification.model.NotificationType;
import io.swkoreatech.kosp.global.dto.PageMeta;
import io.swkoreatech.kosp.global.util.RsqlUtils;
import io.swkoreatech.kosp.infra.email.eventlistener.event.TeamInvitePendingSendEvent;
import io.swkoreatech.kosp.infra.email.eventlistener.event.TeamInviteSendEvent;
import lombok.RequiredArgsConstructor;

/**
 * 팀 서비스.
 * 팀의 CRUD, 멤버 관리, 초대 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    /** 코리아텍 학교 이메일 도메인 (미가입자 초대 시 형식 검증용). */
    private static final String KUT_EMAIL_DOMAIN = "@koreatech.ac.kr";

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamInviteRepository teamInviteRepository;
    private final PendingTeamInviteRepository pendingTeamInviteRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 팀을 생성하고 요청자를 팀장으로 등록한다.
     *
     * @param user    팀 생성 요청자
     * @param request 팀 생성 요청
     * @return 생성된 팀 ID
     */
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

    /**
     * 팀 상세 정보를 조회한다.
     *
     * @param teamId 팀 ID
     * @return 팀 상세 응답
     */
    public TeamDetailResponse getTeam(Long teamId) {
        Team team = teamRepository.getById(teamId);
        List<TeamInvite> pendingInvites = teamInviteRepository
            .findAllByTeamAndStatusAndIsDeletedFalse(team, TeamInvite.InviteStatus.PENDING);
        return TeamDetailResponse.from(team, pendingInvites);
    }

    /**
     * 팀 목록을 검색/필터링하여 조회한다.
     *
     * @param search   검색어 (팀 이름)
     * @param rsql     RSQL 필터 문자열
     * @param pageable 페이징 정보
     * @return 팀 목록 응답
     */
    public TeamListResponse getList(String search, String rsql, Pageable pageable) {
        Specification<Team> spec = createSpecification(search, rsql);
        Page<Team> page = teamRepository.findAll(spec, pageable);
        List<TeamResponse> teams = page.getContent().stream()
            .map(team -> TeamResponse.from(team, getLeader(team)))
            .toList();
        return TeamListResponse.from(teams, PageMeta.from(page));
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

    /**
     * 팀 정보를 수정한다. 팀장만 수정 가능하다.
     *
     * @param teamId  팀 ID
     * @param user    요청 사용자
     * @param request 팀 수정 요청
     */
    @Transactional
    public void update(Long teamId, User user, TeamUpdateRequest request) {
        Team team = teamRepository.getById(teamId);
        validateLeader(team, user);

        team.update(request.name(), request.description(), request.imageUrl());
    }

    /**
     * 팀을 삭제한다. 팀장만 삭제 가능하며 모든 멤버와 초대도 함께 삭제된다.
     *
     * @param teamId 팀 ID
     * @param user   요청 사용자
     */
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

    /**
     * 팀원을 이메일로 초대한다. 팀장만 초대 가능하다.
     *
     * @param teamId    팀 ID
     * @param user      요청 사용자 (팀장)
     * @param request   초대 요청
     * @param clientUrl 클라이언트 기본 URL
     */
    @Transactional
    public void inviteMember(Long teamId, User user, TeamInviteRequest request, String clientUrl) {
        Team team = teamRepository.getById(teamId);
        validateManager(team, user);

        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

        Optional<User> inviteeOpt = userRepository.findByKutEmail(request.email());
        if (inviteeOpt.isPresent()) {
            // 이미 가입된 사용자 → 즉시 실제 초대
            inviteRegisteredMember(team, user, inviteeOpt.get(), expiresAt, clientUrl);
        } else {
            // 미가입자(코리아텍 이메일) → 대기 초대 생성 + 회원가입 유도 메일
            invitePendingMember(team, user, request.email(), expiresAt, clientUrl);
        }
    }

    /**
     * 이미 가입된 사용자를 팀에 초대한다.
     */
    private void inviteRegisteredMember(Team team, User inviter, User invitee, Instant expiresAt, String clientUrl) {
        if (teamMemberRepository.existsByTeamAndUserAndIsDeletedFalse(team, invitee)) {
            throw new GlobalException(ExceptionMessage.TEAM_ALREADY_JOINED);
        }

        // (team_id, invitee_id) 유니크 제약이 있으므로, 기존 초대 행이 있으면 재사용(reopen)한다.
        // 없으면 새로 발급한다. (취소/거절/만료된 초대를 다시 보낼 때 INSERT 충돌 방지)
        TeamInvite invite = teamInviteRepository.findByTeamAndInvitee(team, invitee)
            .map(existing -> {
                // 3회 이상 거절되었다면 마지막 발송 시각으로부터 24시간이 지나야 재초대할 수 있다.
                if (existing.isReinviteBlocked()) {
                    throw new GlobalException(ExceptionMessage.INVITE_REJECTED_TOO_MANY);
                }
                existing.reopen(inviter, expiresAt);
                return existing;
            })
            .orElseGet(() -> teamInviteRepository.save(
                TeamInvite.builder()
                    .team(team)
                    .inviter(inviter)
                    .invitee(invitee)
                    .expiresAt(expiresAt)
                    .build()
            ));

        eventPublisher.publishEvent(new TeamInviteSendEvent(
            invitee.getKutEmail(),
            invitee.getName(),
            team.getName(),
            inviter.getName(),
            invite.getId(),
            clientUrl
        ));

        eventPublisher.publishEvent(NotificationEvent.of(
            invitee.getId(),
            NotificationType.TEAM_INVITED,
            team.getName() + " 팀에 초대되었습니다",
            inviter.getName() + "님이 " + team.getName() + " 팀에 초대했습니다",
            invite.getId()
        ));
    }

    /**
     * 미가입 코리아텍 이메일 사용자를 팀에 초대한다(대기 초대 생성 + 가입 유도 메일).
     *
     * <p>초대 이메일은 코리아텍 형식이어야 한다. 초대받은 사람이 이 이메일로 회원가입을
     * 완료하면 {@link #linkPendingInvitesForNewUser(Long)}에서 실제 초대로 전환된다.</p>
     */
    private void invitePendingMember(Team team, User inviter, String email, Instant expiresAt, String clientUrl) {
        validateKoreatechEmail(email);

        // (team_id, email) 유니크 제약. 기존 대기 초대가 있으면 재사용(reopen)한다.
        pendingTeamInviteRepository.findByTeamAndEmail(team, email)
            .map(existing -> {
                existing.reopen(inviter, expiresAt);
                return existing;
            })
            .orElseGet(() -> pendingTeamInviteRepository.save(
                PendingTeamInvite.builder()
                    .team(team)
                    .inviter(inviter)
                    .email(email)
                    .expiresAt(expiresAt)
                    .build()
            ));

        eventPublisher.publishEvent(new TeamInvitePendingSendEvent(
            email,
            team.getName(),
            inviter.getName(),
            clientUrl
        ));
    }

    /** 초대 이메일이 코리아텍 학교 이메일 형식인지 검증한다. */
    private void validateKoreatechEmail(String email) {
        if (email == null || !email.toLowerCase().endsWith(KUT_EMAIL_DOMAIN)) {
            throw new GlobalException(ExceptionMessage.INVALID_EMAIL_ADDRESS);
        }
    }

    /**
     * 새로 가입한 사용자의 이메일로 발송된 대기 초대들을 실제 팀 초대로 전환한다.
     *
     * <p>회원가입 완료 후 {@code PendingTeamInviteLinkListener}가 호출한다. 각 대기 초대에 대해
     * 실제 {@link TeamInvite}(PENDING)를 생성하고 알림을 발행하여, 가입 직후 팀 초대를 받을 수 있게 한다.
     * 이미 팀 멤버이거나 만료된 대기 초대는 소비 처리만 하고 건너뛴다.</p>
     *
     * @param userId 새로 가입한 사용자 ID
     */
    @Transactional
    public void linkPendingInvitesForNewUser(Long userId) {
        User user = userRepository.getById(userId);
        List<PendingTeamInvite> pendings =
            pendingTeamInviteRepository.findAllByEmailAndIsDeletedFalse(user.getKutEmail());
        if (pendings.isEmpty()) {
            return;
        }

        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        for (PendingTeamInvite pending : pendings) {
            Team team = pending.getTeam();
            User inviter = pending.getInviter();

            // 만료됐거나 이미 팀 멤버면 실제 초대 없이 소비 처리만
            if (pending.isExpired() || teamMemberRepository.existsByTeamAndUserAndIsDeletedFalse(team, user)) {
                pending.consume();
                continue;
            }

            TeamInvite invite = teamInviteRepository.findByTeamAndInvitee(team, user)
                .map(existing -> {
                    existing.reopen(inviter, expiresAt);
                    return existing;
                })
                .orElseGet(() -> teamInviteRepository.save(
                    TeamInvite.builder()
                        .team(team)
                        .inviter(inviter)
                        .invitee(user)
                        .expiresAt(expiresAt)
                        .build()
                ));

            pending.consume();

            eventPublisher.publishEvent(NotificationEvent.of(
                user.getId(),
                NotificationType.TEAM_INVITED,
                team.getName() + " 팀에 초대되었습니다",
                inviter.getName() + "님이 " + team.getName() + " 팀에 초대했습니다",
                invite.getId()
            ));
        }
    }

    /**
     * 팀 초대를 수락한다. 피초대자만 수락 가능하다.
     *
     * @param inviteId 초대 ID
     * @param user     요청 사용자 (피초대자)
     */
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

    /**
     * 팀 초대를 거절한다. 피초대자만 거절 가능하다.
     *
     * @param inviteId 초대 ID
     * @param user     요청 사용자 (피초대자)
     */
    @Transactional
    public void rejectInvite(Long inviteId, User user) {
        TeamInvite invite = teamInviteRepository.findByIdAndIsDeletedFalse(inviteId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        if (!invite.getInvitee().getId().equals(user.getId())) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }

        // 거절 시 누적 거절 횟수를 증가시킨다 (3회 이상이면 재초대 24시간 제한 적용).
        invite.reject();
    }

    /**
     * 팀원을 제명한다. 팀장 또는 관리자만 제명 가능하며 팀장은 제명할 수 없다.
     *
     * @param teamId       팀 ID
     * @param user         요청 사용자 (팀장/관리자)
     * @param targetUserId 제명 대상 사용자 ID
     */
    @Transactional
    public void removeMember(Long teamId, User user, Long targetUserId) {
        Team team = teamRepository.getById(teamId);
        validateManager(team, user);

        if (user.getId().equals(targetUserId)) {
            throw new GlobalException(ExceptionMessage.LEADER_CANNOT_LEAVE);
        }

        User targetUser = userRepository.getById(targetUserId);
        TeamMember member = teamMemberRepository.findByTeamAndUserAndIsDeletedFalse(team, targetUser)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        if (member.getRole() == TeamRole.LEADER) {
            throw new GlobalException(ExceptionMessage.LEADER_CANNOT_LEAVE);
        }

        member.delete();
    }

    /**
     * 본인이 자발적으로 팀에서 나간다. 팀장은 탈퇴할 수 없다.
     *
     * @param teamId 팀 ID
     * @param user   요청 사용자 (본인)
     */
    @Transactional
    public void leaveTeam(Long teamId, User user) {
        Team team = teamRepository.getById(teamId);
        TeamMember member = teamMemberRepository.findByTeamAndUserAndIsDeletedFalse(team, user)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        if (member.getRole() == TeamRole.LEADER) {
            throw new GlobalException(ExceptionMessage.LEADER_CANNOT_LEAVE);
        }

        member.delete();
    }

    /**
     * 발송한 초대를 취소한다. 팀장 또는 관리자만 취소 가능하다.
     *
     * @param inviteId 초대 ID
     * @param user     요청 사용자 (팀장/관리자)
     */
    @Transactional
    public void cancelInvite(Long inviteId, User user) {
        TeamInvite invite = teamInviteRepository.findByIdAndIsDeletedFalse(inviteId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        validateManager(invite.getTeam(), user);

        invite.cancel();
    }

    /**
     * 팀원의 역할(권한)을 변경한다. 팀장만 위임/회수할 수 있다.
     *
     * <p>부여 가능한 역할은 ADMIN, MEMBER이며 LEADER 위임은 허용하지 않는다.
     * 또한 다른 팀장의 역할은 변경할 수 없다.</p>
     *
     * @param teamId       팀 ID
     * @param user         요청 사용자 (팀장)
     * @param targetUserId 대상 사용자 ID
     * @param role         변경할 역할
     */
    @Transactional
    public void changeMemberRole(Long teamId, User user, Long targetUserId, TeamRole role) {
        Team team = teamRepository.getById(teamId);
        validateLeader(team, user);

        if (role == TeamRole.LEADER) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }

        User targetUser = userRepository.getById(targetUserId);
        TeamMember member = teamMemberRepository.findByTeamAndUserAndIsDeletedFalse(team, targetUser)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        if (member.getRole() == TeamRole.LEADER) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }

        member.changeRole(role);
    }

    private void validateLeader(Team team, User user) {
        TeamMember member = teamMemberRepository.findByTeamAndUserAndIsDeletedFalse(team, user)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.FORBIDDEN));

        if (member.getRole() != TeamRole.LEADER) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }

    /** 팀장 또는 관리자(ADMIN) 권한을 검증한다. (초대/제명/초대취소 공통) */
    private void validateManager(Team team, User user) {
        TeamMember member = teamMemberRepository.findByTeamAndUserAndIsDeletedFalse(team, user)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.FORBIDDEN));

        if (member.getRole() != TeamRole.LEADER && member.getRole() != TeamRole.ADMIN) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }

    /**
     * 사용자가 소속된 모든 팀을 조회한다.
     *
     * @param user 요청 사용자
     * @return 소속 팀 목록
     */
    public List<TeamDetailResponse> getMyTeams(User user) {
        return teamMemberRepository.findAllByUserAndIsDeletedFalse(user).stream()
            .map(member -> TeamDetailResponse.from(member.getTeam()))
            .toList();
    }

    /**
     * 사용자가 소속된 팀 정보를 조회한다.
     *
     * @param user 요청 사용자
     * @return 소속 팀 상세 응답
     */
    public TeamDetailResponse getMyTeam(User user) {
        TeamMember member = teamMemberRepository.findByUser(user)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
        return TeamDetailResponse.from(member.getTeam());
    }
}
