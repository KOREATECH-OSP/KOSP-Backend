package io.swkoreatech.kosp.domain.admin.title.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.admin.title.api.AdminTitleApi;
import io.swkoreatech.kosp.domain.admin.title.dto.request.AdminTitleGrantRequest;
import io.swkoreatech.kosp.domain.admin.title.dto.request.AdminTitleRevokeRequest;
import io.swkoreatech.kosp.domain.admin.title.dto.request.AdminTitleUpdateImageRequest;
import io.swkoreatech.kosp.domain.admin.title.dto.response.AdminTitleBatchLogListResponse;
import io.swkoreatech.kosp.domain.admin.title.service.AdminTitleService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 칭호 관리 컨트롤러.
 * <p>{@link AdminTitleApi}를 구현하여 칭호 수동 지급/회수 및 배치 로그 조회 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class AdminTitleController implements AdminTitleApi {

    private final AdminTitleService adminTitleService;

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:titles:create", description = "칭호 수동 지급")
    public ResponseEntity<Void> grantTitle(AdminTitleGrantRequest request, User admin) {
        adminTitleService.grantTitle(request, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:titles:delete", description = "칭호 회수")
    public ResponseEntity<Void> revokeTitle(AdminTitleRevokeRequest request, User admin) {
        adminTitleService.revokeTitle(request, admin.getId());
        return ResponseEntity.ok().build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:titles:update", description = "칭호 아이콘 URL 수정")
    public ResponseEntity<Void> updateTitleImage(Long titleId, AdminTitleUpdateImageRequest request) {
        adminTitleService.updateTitleImage(titleId, request.iconUrl());
        return ResponseEntity.ok().build();
    }

    /** {@inheritDoc} */
    @Override
    @Permit(name = "admin:titles:read", description = "배치 실행 이력 조회")
    public ResponseEntity<AdminTitleBatchLogListResponse> getBatchLogs(Pageable pageable) {
        return ResponseEntity.ok(adminTitleService.getBatchLogs(pageable));
    }
}
