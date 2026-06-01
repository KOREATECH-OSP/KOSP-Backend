package io.swkoreatech.kosp.domain.admin.season.api;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.admin.season.dto.request.AdminSeasonProjectCreateRequest;
import io.swkoreatech.kosp.domain.admin.season.dto.request.AdminSeasonProjectMemberAddRequest;
import io.swkoreatech.kosp.domain.admin.season.dto.response.AdminSeasonProjectListResponse;
import io.swkoreatech.kosp.domain.admin.season.dto.response.AdminSeasonProjectMemberListResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

/**
 * 관리자 전용 시즌 프로젝트 관리 API 인터페이스.
 */
@Tag(name = "Admin - Season", description = "관리자 전용 시즌 프로젝트 점수 관리 API")
@RequestMapping("/v1/admin/seasons")
public interface AdminSeasonApi {

    /**
     * 시즌 프로젝트를 오픈한다.
     */
    @Operation(summary = "시즌 프로젝트 오픈", description = "관리자가 시즌 프로젝트를 생성합니다. 참여자를 등록한 후 종료 시 일괄 점수 지급됩니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping("/{seasonId}/projects")
    ResponseEntity<Void> openProject(
        @PathVariable Long seasonId,
        @RequestBody @Valid AdminSeasonProjectCreateRequest request,
        @AuthUser User admin
    );

    /**
     * 프로젝트에 참여자를 추가한다.
     */
    @Operation(summary = "프로젝트 참여자 추가", description = "프로젝트에 유저를 참여자로 등록합니다.")
    @ApiResponse(responseCode = "201", description = "추가 성공")
    @ApiResponse(responseCode = "400", description = "이미 종료된 프로젝트")
    @ApiResponse(responseCode = "409", description = "이미 등록된 참여자")
    @PostMapping("/projects/{projectId}/members")
    ResponseEntity<Void> addMember(
        @PathVariable Long projectId,
        @RequestBody @Valid AdminSeasonProjectMemberAddRequest request
    );

    /**
     * 프로젝트를 종료하고 참여자에게 점수를 일괄 지급한다.
     */
    @Operation(summary = "프로젝트 종료 및 점수 일괄 지급", description = "프로젝트를 종료하고 참여자에게 레벨+역할 보너스 점수를 일괄 지급합니다.")
    @ApiResponse(responseCode = "200", description = "종료 및 점수 지급 성공")
    @ApiResponse(responseCode = "400", description = "이미 종료된 프로젝트")
    @PostMapping("/projects/{projectId}/close")
    ResponseEntity<Void> closeProject(
        @PathVariable Long projectId,
        @AuthUser User admin
    );

    /**
     * 시즌 프로젝트 목록을 조회한다.
     */
    @Operation(summary = "시즌 프로젝트 목록 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{seasonId}/projects")
    ResponseEntity<AdminSeasonProjectListResponse> getProjects(
        @PathVariable Long seasonId,
        @PageableDefault(size = 20) Pageable pageable
    );

    /**
     * 프로젝트 참여자 목록을 조회한다.
     */
    @Operation(summary = "프로젝트 참여자 목록 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/projects/{projectId}/members")
    ResponseEntity<AdminSeasonProjectMemberListResponse> getMembers(
        @PathVariable Long projectId
    );
}
