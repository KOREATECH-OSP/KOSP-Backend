package io.swkoreatech.kosp.domain.admin.report.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.admin.content.service.AdminContentService;
import io.swkoreatech.kosp.domain.admin.report.dto.request.ReportProcessRequest;
import io.swkoreatech.kosp.domain.admin.report.dto.response.ReportResponse;
import io.swkoreatech.kosp.domain.report.model.Report;
import io.swkoreatech.kosp.domain.report.model.enums.ReportStatus;
import io.swkoreatech.kosp.domain.report.model.enums.ReportTargetType;
import io.swkoreatech.kosp.domain.report.repository.ReportRepository;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

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
            throw new GlobalException(ExceptionMessage.BAD_REQUEST);
        }

        if (request.action() == ReportProcessRequest.Action.REJECT) {
            report.process(ReportStatus.REJECTED);
            return;
        }

        if (request.action() == ReportProcessRequest.Action.DELETE_CONTENT) {
            deleteContent(report);
            report.process(ReportStatus.ACCEPTED);
        }
    }

    private void deleteContent(Report report) {
        if (report.getTargetType() == ReportTargetType.ARTICLE) {
            adminContentService.deleteArticle(report.getTargetId());
            return;
        }

        if (report.getTargetType() == ReportTargetType.COMMENT) {
            adminContentService.deleteComment(report.getTargetId());
        }
    }
}
