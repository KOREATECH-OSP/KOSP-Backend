package io.swkoreatech.kosp.domain.admin.report.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.domain.admin.content.service.AdminContentService;
import io.swkoreatech.kosp.domain.admin.report.dto.request.ReportProcessRequest;
import io.swkoreatech.kosp.domain.admin.report.dto.response.ReportResponse;
import io.swkoreatech.kosp.domain.report.model.Report;
import io.swkoreatech.kosp.domain.report.model.enums.ReportStatus;
import io.swkoreatech.kosp.domain.report.model.enums.ReportTargetType;
import io.swkoreatech.kosp.domain.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 신고 관리 서비스.
 * <p>신고 목록 조회 및 신고 처리 (콘텐츠 삭제 또는 기각) 비즈니스 로직을 처리한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final AdminContentService adminContentService;

    /**
     * 모든 신고 목록을 조회한다.
     *
     * @return 신고 응답 DTO 목록
     */
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll()
            .stream()
            .map(ReportResponse::from)
            .toList();
    }

    /**
     * 신고를 처리한다.
     *
     * @param reportId 신고 식별자
     * @param request  신고 처리 요청 DTO (삭제 또는 기각)
     * @throws GlobalException 이미 처리된 신고인 경우
     */
    @Transactional
    public void processReport(Long reportId, ReportProcessRequest request) {
        Report report = reportRepository.getById(reportId);

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
