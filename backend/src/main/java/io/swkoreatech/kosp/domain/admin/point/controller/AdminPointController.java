package io.swkoreatech.kosp.domain.admin.point.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.admin.point.api.AdminPointApi;
import io.swkoreatech.kosp.domain.admin.point.dto.request.PointTransactionRequest;
import io.swkoreatech.kosp.domain.admin.point.dto.response.PointHistoryResponse;
import io.swkoreatech.kosp.domain.admin.point.service.AdminPointService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 포인트 관리 컨트롤러.
 * <p>{@link AdminPointApi}를 구현하여 포인트 변경 및 내역 조회 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class AdminPointController implements AdminPointApi {

    private final AdminPointService adminPointService;

    @Override
    @Permit(name = "admin:points:change", description = "포인트 변경")
    public ResponseEntity<Void> changePoint(Long userId, PointTransactionRequest request, User admin) {
        adminPointService.changePoint(userId, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "admin:points:read", description = "포인트 내역 조회")
    public ResponseEntity<PointHistoryResponse> getPointHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        PointHistoryResponse response = adminPointService.getPointHistory(userId, pageable);
        return ResponseEntity.ok(response);
    }
}
