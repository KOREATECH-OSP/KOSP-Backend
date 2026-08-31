package io.swkoreatech.kosp.domain.admin.season.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.admin.season.api.AdminSeasonApi;
import io.swkoreatech.kosp.domain.admin.season.dto.request.AdminSeasonProjectCreateRequest;
import io.swkoreatech.kosp.domain.admin.season.dto.request.AdminSeasonProjectMemberAddRequest;
import io.swkoreatech.kosp.domain.admin.season.dto.request.AdminSeasonProjectMemberRoleChangeRequest;
import io.swkoreatech.kosp.domain.admin.season.dto.response.AdminCurrentSeasonResponse;
import io.swkoreatech.kosp.domain.admin.season.dto.response.AdminSeasonProjectListResponse;
import io.swkoreatech.kosp.domain.admin.season.dto.response.AdminSeasonProjectMemberListResponse;
import io.swkoreatech.kosp.domain.admin.season.service.AdminSeasonProjectService;
import io.swkoreatech.kosp.domain.season.service.SeasonRankingBatchService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 시즌 프로젝트 관리 컨트롤러.
 * <p>{@link AdminSeasonApi}를 구현하여 시즌 프로젝트 오픈/참여자 등록/종료 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class AdminSeasonController implements AdminSeasonApi {

    private final AdminSeasonProjectService adminSeasonProjectService;
    private final SeasonRankingBatchService seasonRankingBatchService;

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:seasons:read", description = "현재 활성 시즌 조회")
    public ResponseEntity<AdminCurrentSeasonResponse> getCurrentSeason() {
        return ResponseEntity.ok(adminSeasonProjectService.getCurrentSeason());
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:seasons:create", description = "시즌 프로젝트 생성")
    public ResponseEntity<Void> openProject(Long seasonId, AdminSeasonProjectCreateRequest request, User admin) {
        adminSeasonProjectService.openProject(seasonId, request, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:seasons:update", description = "프로젝트 참여자 추가")
    public ResponseEntity<Void> addMember(Long projectId, AdminSeasonProjectMemberAddRequest request) {
        adminSeasonProjectService.addMember(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:seasons:close", description = "프로젝트 종료 및 점수 지급")
    public ResponseEntity<Void> closeProject(Long projectId, User admin) {
        adminSeasonProjectService.closeProject(projectId, admin.getId());
        return ResponseEntity.ok().build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:seasons:read", description = "시즌 프로젝트 목록 조회")
    public ResponseEntity<AdminSeasonProjectListResponse> getProjects(Long seasonId, Pageable pageable) {
        return ResponseEntity.ok(adminSeasonProjectService.getProjects(seasonId, pageable));
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:seasons:read", description = "프로젝트 참여자 목록 조회")
    public ResponseEntity<AdminSeasonProjectMemberListResponse> getMembers(Long projectId) {
        return ResponseEntity.ok(adminSeasonProjectService.getMembers(projectId));
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:seasons:update", description = "프로젝트 참여자 역할 변경")
    public ResponseEntity<Void> changeMemberRole(Long memberId, AdminSeasonProjectMemberRoleChangeRequest request) {
        adminSeasonProjectService.changeMemberRole(memberId, request);
        return ResponseEntity.ok().build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:seasons:update", description = "프로젝트 참여자 삭제")
    public ResponseEntity<Void> removeMember(Long memberId) {
        adminSeasonProjectService.removeMember(memberId);
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:seasons:batch", description = "시즌 랭킹 배치 강제 실행")
    public ResponseEntity<Void> runRankingBatch() {
        seasonRankingBatchService.runRankingBatch();
        return ResponseEntity.ok().build();
    }
}
