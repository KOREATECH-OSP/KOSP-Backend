package io.swkoreatech.kosp.domain.community.team.api;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.team.dto.request.TeamCreateRequest;
import io.swkoreatech.kosp.domain.community.team.dto.request.TeamInviteRequest;
import io.swkoreatech.kosp.domain.community.team.dto.request.TeamRoleUpdateRequest;
import io.swkoreatech.kosp.domain.community.team.dto.request.TeamUpdateRequest;
import io.swkoreatech.kosp.domain.community.team.dto.response.InviteAvailabilityResponse;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamDetailResponse;
import io.swkoreatech.kosp.domain.community.team.dto.response.TeamListResponse;
import io.swkoreatech.kosp.global.host.ClientURL;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

/**
 * 팀 API 인터페이스.
 * 팀의 CRUD, 멤버 관리, 초대 관련 엔드포인트를 정의한다.
 */
@Tag(name = "Team", description = "팀 관리 API")
public interface TeamApi {

    @Operation(summary = "팀 목록 조회", description = "팀 목록을 조회합니다.")
    @GetMapping("/v1/teams")
    ResponseEntity<TeamListResponse> getList(
        @RequestParam(required = false, defaultValue = "") String search,
        @Parameter(description = "RSQL 필터 (예: isDeleted==false, name==*test*)")
        @RequestParam(required = false) String rsql,
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

    @Operation(summary = "내 팀 조회", description = "인증된 사용자가 소속된 모든 팀을 조회합니다.")
    @GetMapping("/v1/teams/me")
    ResponseEntity<List<TeamDetailResponse>> getMyTeams(
        @Parameter(hidden = true) @AuthUser User user
    );

    @Operation(summary = "팀 정보 수정", description = "팀장이 팀 정보를 수정합니다.")
    @PutMapping("/v1/teams/{teamId}")
    ResponseEntity<Void> update(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long teamId,
        @RequestBody @Valid TeamUpdateRequest request
    );

    @Operation(summary = "팀 삭제", description = "팀장이 팀을 삭제합니다. 팀의 모든 멤버와 초대도 함께 삭제됩니다.")
    @DeleteMapping("/v1/teams/{teamId}")
    ResponseEntity<Void> deleteTeam(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long teamId
    );

    @Operation(summary = "팀원 초대", description = "팀장이 새로운 팀원을 이메일로 초대합니다.")
    @PostMapping("/v1/teams/{teamId}/invites")
    ResponseEntity<Void> inviteMember(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long teamId,
        @RequestBody @Valid TeamInviteRequest request,
        @Parameter(hidden = true) @ClientURL String clientUrl
    );

    @Operation(
        summary = "초대 가능 여부 조회",
        description = "해당 이메일의 사용자를 지금 초대할 수 있는지 확인합니다. "
            + "반복 거절로 제한된 경우 누적 거절 횟수·최근 거절 시각·제한 종료 시각을 함께 반환합니다."
    )
    @GetMapping("/v1/teams/{teamId}/invites/availability")
    ResponseEntity<InviteAvailabilityResponse> getInviteAvailability(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long teamId,
        @Parameter(description = "피초대자 KUT 이메일") @RequestParam String email
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

    @Operation(summary = "팀원 제명", description = "팀장 또는 관리자가 팀원을 제명합니다. 팀장은 제명할 수 없습니다.")
    @DeleteMapping("/v1/teams/{teamId}/members/{userId}")
    ResponseEntity<Void> removeMember(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long teamId,
        @PathVariable Long userId
    );

    @Operation(summary = "팀 탈퇴", description = "본인이 자발적으로 팀에서 나갑니다. 팀장은 탈퇴할 수 없습니다.")
    @DeleteMapping("/v1/teams/{teamId}/members/me")
    ResponseEntity<Void> leaveTeam(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long teamId
    );

    @Operation(summary = "초대 취소", description = "팀장 또는 관리자가 발송한 초대를 취소합니다.")
    @DeleteMapping("/v1/teams/invites/{inviteId}")
    ResponseEntity<Void> cancelInvite(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long inviteId
    );

    @Operation(summary = "팀원 권한 변경", description = "팀장이 팀원에게 관리자 권한을 위임하거나 회수합니다.")
    @PatchMapping("/v1/teams/{teamId}/members/{userId}/role")
    ResponseEntity<Void> changeMemberRole(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long teamId,
        @PathVariable Long userId,
        @RequestBody @Valid TeamRoleUpdateRequest request
    );
}
