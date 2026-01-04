package kr.ac.koreatech.sw.kosp.domain.admin.service;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.admin.report.dto.request.ReportProcessRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.report.dto.response.ReportResponse;
import kr.ac.koreatech.sw.kosp.domain.report.model.Report;
import kr.ac.koreatech.sw.kosp.domain.report.model.enums.ReportStatus;
import kr.ac.koreatech.sw.kosp.domain.report.model.enums.ReportTargetType;
import kr.ac.koreatech.sw.kosp.domain.report.repository.ReportRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final AdminContentService adminContentService;

    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll()
            .stream()
            .map(ReportResponse::from)
            .toList();
    }

    @Transactional
    public void processReport(Long reportId, ReportProcessRequest request) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new GlobalException(ExceptionMessage.BAD_REQUEST); // 이미 처리된 신고
        }

        if (request.action() == ReportProcessRequest.Action.DELETE_CONTENT) {
            deleteContent(report);
            report.process(ReportStatus.ACCEPTED);
        } else if (request.action() == ReportProcessRequest.Action.REJECT) {
            report.process(ReportStatus.REJECTED);
        }
    }

    private void deleteContent(Report report) {
        if (report.getTargetType() == ReportTargetType.ARTICLE) {
            adminContentService.deleteArticle(report.getTargetId());
        } else if (report.getTargetType() == ReportTargetType.COMMENT) {
            adminContentService.deleteComment(report.getTargetId());
        }
    }
}
