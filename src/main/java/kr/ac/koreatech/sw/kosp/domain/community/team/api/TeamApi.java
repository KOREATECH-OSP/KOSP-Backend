package kr.ac.koreatech.sw.kosp.domain.community.team.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamInviteRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamDetailResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamListResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.host.ClientURL;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;

@Tag(name = "Team", description = "팀 관리 API")
public interface TeamApi {

    @Operation(summary = "팀 목록 조회", description = "팀 목록을 조회합니다.")
    @GetMapping("/v1/teams")
    ResponseEntity<TeamListResponse> getList(
        @RequestParam(required = false, defaultValue = "") String search,
        @Parameter(hidden = true) Pageable pageable
    );

    @Operation(summary = "팀 생성", description = "새로운 팀을 생성합니다.")
    @PostMapping("/v1/teams")
    ResponseEntity<Void> create(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid TeamCreateRequest request
    );

    @Operation(summary = "팀 상세 조회", description = "팀 상세 정보를 조회합니다.")
    @GetMapping("/v1/teams/{teamId}")
    ResponseEntity<TeamDetailResponse> getTeam(
        @PathVariable Long teamId
    );

    @Operation(summary = "내 팀 조회", description = "인증된 사용자가 소속된 팀을 조회합니다.")
    @GetMapping("/v1/teams/me")
    ResponseEntity<TeamDetailResponse> getMyTeam(
        @Parameter(hidden = true) @AuthUser User user
    );

    @Operation(summary = "팀 정보 수정", description = "팀장이 팀 정보를 수정합니다.")
    @PutMapping("/v1/teams/{teamId}")
    ResponseEntity<Void> update(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long teamId,
        @RequestBody @Valid TeamUpdateRequest request
    );

    @Operation(summary = "팀원 초대", description = "팀장이 새로운 팀원을 이메일로 초대합니다.")
    @PostMapping("/v1/teams/{teamId}/invites")
    ResponseEntity<Void> inviteMember(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long teamId,
        @RequestBody @Valid TeamInviteRequest request,
        @Parameter(hidden = true) @ClientURL String clientUrl
    );

    @Operation(summary = "초대 수락", description = "초대받은 사용자가 초대를 수락합니다.")
    @PostMapping("/v1/teams/invites/{inviteId}/accept")
    ResponseEntity<Void> acceptInvite(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long inviteId
    );

    @Operation(summary = "초대 거절", description = "초대받은 사용자가 초대를 거절합니다.")
    @PostMapping("/v1/teams/invites/{inviteId}/reject")
    ResponseEntity<Void> rejectInvite(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long inviteId
    );

    @Operation(summary = "팀원 제명", description = "팀장이 팀원을 제명합니다.")
    @DeleteMapping("/v1/teams/{teamId}/members/{userId}")
    ResponseEntity<Void> removeMember(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long teamId,
        @PathVariable Long userId
    );
}
