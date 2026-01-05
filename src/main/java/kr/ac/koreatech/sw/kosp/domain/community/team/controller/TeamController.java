package kr.ac.koreatech.sw.kosp.domain.community.team.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.community.team.api.TeamApi;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamDetailResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.service.TeamService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
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
    public ResponseEntity<TeamDetailResponse> getMyTeam(@AuthUser User user) {
        TeamDetailResponse response = teamService.getMyTeam(user);
        return ResponseEntity.ok(response);
    }
}
