package io.swkoreatech.kosp.domain.community.team.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.community.team.api.TeamInviteApi;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamInviteResponse;
import io.swkoreatech.kosp.domain.community.team.service.TeamInviteService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TeamInviteController implements TeamInviteApi {

    private final TeamInviteService teamInviteService;

    @Override
    @Permit(permitAll = true, description = "초대 상세 조회")
    public ResponseEntity<TeamInviteResponse> getInvite(Long inviteId) {
        TeamInviteResponse response = teamInviteService.getInvite(inviteId);
        return ResponseEntity.ok(response);
    }
}
