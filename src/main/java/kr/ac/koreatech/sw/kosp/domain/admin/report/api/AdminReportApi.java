package kr.ac.koreatech.sw.kosp.domain.admin.report.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.report.dto.request.ReportProcessRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.report.dto.response.ReportResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Admin - Report", description = "관리자 전용 신고 관리 API")
@RequestMapping("/v1/admin/reports")
public interface AdminReportApi {

    @Operation(summary = "신고 목록 조회", description = "접수된 모든 신고 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<List<ReportResponse>> getAllReports();

    @Operation(summary = "신고 처리", description = "신고를 처리(삭제 또는 기각)합니다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @PostMapping("/{reportId}")
    ResponseEntity<Void> processReport(
        @Parameter(description = "신고 ID") @PathVariable Long reportId,
        @RequestBody @Valid ReportProcessRequest request
    );
}
