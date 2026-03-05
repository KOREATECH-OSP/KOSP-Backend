package io.swkoreatech.kosp.domain.admin.report.controller;

import io.swkoreatech.kosp.domain.admin.report.api.AdminReportApi;
import io.swkoreatech.kosp.domain.admin.report.dto.request.ReportProcessRequest;
import io.swkoreatech.kosp.domain.admin.report.dto.response.ReportResponse;
import io.swkoreatech.kosp.domain.admin.report.service.AdminReportService;
import io.swkoreatech.kosp.global.security.annotation.Permit;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 신고 관리 컨트롤러.
 * <p>{@link AdminReportApi}를 구현하여 신고 목록 조회 및 처리 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class AdminReportController implements AdminReportApi {

    private final AdminReportService adminReportService;

    @Override
    @Permit(name = "admin:reports:read", description = "신고 목록 조회")
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        return ResponseEntity.ok(adminReportService.getAllReports());
    }

    @Override
    @Permit(name = "admin:reports:process", description = "신고 처리")
    public ResponseEntity<Void> processReport(Long reportId, ReportProcessRequest request) {
        adminReportService.processReport(reportId, request);
        return ResponseEntity.ok().build();
    }
}
