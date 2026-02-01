package io.swkoreatech.kosp.domain.community.team.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamInviteResponse;

@Tag(name = "TeamInvite", description = "팀 초대 관리 API")
public interface TeamInviteApi {

    @Operation(summary = "초대 상세 조회", description = "초대 ID로 상세 정보를 조회합니다.")
    @GetMapping("/v1/teams/invites/{inviteId}")
    ResponseEntity<TeamInviteResponse> getInvite(@PathVariable Long inviteId);
}
