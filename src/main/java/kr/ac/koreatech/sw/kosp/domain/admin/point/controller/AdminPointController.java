package kr.ac.koreatech.sw.kosp.domain.admin.point.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.admin.point.api.AdminPointApi;
import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.request.PointTransactionRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.response.PointHistoryResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.response.PointTransactionResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.point.service.AdminPointService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminPointController implements AdminPointApi {

    private final AdminPointService adminPointService;

    @Override
    @Permit(name = "admin:points:change", description = "포인트 변경")
    public ResponseEntity<PointTransactionResponse> changePoint(Long userId, PointTransactionRequest request, User admin) {
        PointTransactionResponse response = adminPointService.changePoint(userId, request, admin);
        return ResponseEntity.ok(response);
    }

    @Override
    @Permit(name = "admin:points:read", description = "포인트 내역 조회")
    public ResponseEntity<PointHistoryResponse> getPointHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        PointHistoryResponse response = adminPointService.getPointHistory(userId, pageable);
        return ResponseEntity.ok(response);
    }
}
