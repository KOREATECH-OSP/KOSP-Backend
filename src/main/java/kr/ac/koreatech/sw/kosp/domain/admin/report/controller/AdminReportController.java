package kr.ac.koreatech.sw.kosp.domain.admin.report.controller;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.report.api.AdminReportApi;
import kr.ac.koreatech.sw.kosp.domain.admin.report.dto.request.ReportProcessRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.report.dto.response.ReportResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.service.AdminReportService;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
