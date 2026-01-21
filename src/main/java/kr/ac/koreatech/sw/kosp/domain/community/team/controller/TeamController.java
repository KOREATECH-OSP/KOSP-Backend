package kr.ac.koreatech.sw.kosp.domain.community.team.controller;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.community.team.api.TeamApi;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamInviteRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamDetailResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.service.TeamService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.host.ClientURL;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TeamController implements TeamApi {

    private final TeamService teamService;

    @Override
    @Permit(permitAll = true, description = "팀 목록 조회")
    public ResponseEntity<TeamListResponse> getList(String search, Pageable pageable) {
        TeamListResponse response = teamService.getList(search, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @Permit(name = "team:create", description = "팀 생성")
    public ResponseEntity<Void> create(@AuthUser User user, TeamCreateRequest request) {
        Long id = teamService.create(user, request);
        return ResponseEntity.created(URI.create("/v1/teams/" + id)).build();
    }

    @Override
    @Permit(permitAll = true, description = "팀 상세 조회")
    public ResponseEntity<TeamDetailResponse> getTeam(Long teamId) {
        TeamDetailResponse response = teamService.getTeam(teamId);
        return ResponseEntity.ok(response);
    }

    @Override
    @Permit(name = "team:read", description = "내 팀 조회")
    public ResponseEntity<List<TeamDetailResponse>> getMyTeams(@AuthUser User user) {
        List<TeamDetailResponse> response = teamService.getMyTeams(user);
        return ResponseEntity.ok(response);
    }

    @Override
    @Permit(name = "team:update", description = "팀 정보 수정")
    public ResponseEntity<Void> update(@AuthUser User user, Long teamId, TeamUpdateRequest request) {
        teamService.update(teamId, user, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "team:invite", description = "팀원 초대")
    public ResponseEntity<Void> inviteMember(@AuthUser User user, Long teamId, TeamInviteRequest request, @ClientURL String clientUrl) {
        teamService.inviteMember(teamId, user, request, clientUrl);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(permitAll = true, description = "초대 수락")
    public ResponseEntity<Void> acceptInvite(@AuthUser User user, Long inviteId) {
        teamService.acceptInvite(inviteId, user);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(permitAll = true, description = "초대 거절")
    public ResponseEntity<Void> rejectInvite(@AuthUser User user, Long inviteId) {
        teamService.rejectInvite(inviteId, user);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "team:kick", description = "팀원 제명")
    public ResponseEntity<Void> removeMember(@AuthUser User user, Long teamId, Long userId) {
        teamService.removeMember(teamId, user, userId);
        return ResponseEntity.ok().build();
    }
}
