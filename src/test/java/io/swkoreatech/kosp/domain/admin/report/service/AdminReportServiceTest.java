package io.swkoreatech.kosp.domain.admin.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.admin.content.service.AdminContentService;
import io.swkoreatech.kosp.domain.admin.report.dto.request.ReportProcessRequest;
import io.swkoreatech.kosp.domain.admin.report.dto.response.ReportResponse;
import io.swkoreatech.kosp.domain.report.model.Report;
import io.swkoreatech.kosp.domain.report.model.enums.ReportReason;
import io.swkoreatech.kosp.domain.report.model.enums.ReportStatus;
import io.swkoreatech.kosp.domain.report.model.enums.ReportTargetType;
import io.swkoreatech.kosp.domain.report.repository.ReportRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminReportService 단위 테스트")
class AdminReportServiceTest {

    @InjectMocks
    private AdminReportService adminReportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private AdminContentService adminContentService;

    private User createUser(Long id, String name) {
        User user = User.builder()
            .name(name)
            .kutId("2024" + id)
            .kutEmail(name + "@koreatech.ac.kr")
            .password("password")
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Report createReport(Long id, ReportTargetType targetType, ReportStatus status) {
        User reporter = createUser(id, "reporter" + id);
        Report report = Report.builder()
            .reporter(reporter)
            .targetType(targetType)
            .targetId(100L)
            .reason(ReportReason.SPAM)
            .description("테스트 신고")
            .build();
        ReflectionTestUtils.setField(report, "id", id);
        ReflectionTestUtils.setField(report, "status", status);
        return report;
    }

    @Nested
    @DisplayName("getAllReports 메서드")
    class GetAllReportsTest {

        @Test
        @DisplayName("신고 목록이 비어있으면 빈 리스트를 반환한다")
        void returnsEmptyList_whenNoReports() {
            // given
            given(reportRepository.findAll()).willReturn(Collections.emptyList());

            // when
            List<ReportResponse> result = adminReportService.getAllReports();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("신고 목록이 존재하면 ReportResponse 리스트를 반환한다")
        void returnsReportResponseList_whenReportsExist() {
            // given
            Report report1 = createReport(1L, ReportTargetType.ARTICLE, ReportStatus.PENDING);
            Report report2 = createReport(2L, ReportTargetType.COMMENT, ReportStatus.ACCEPTED);
            given(reportRepository.findAll()).willReturn(List.of(report1, report2));

            // when
            List<ReportResponse> result = adminReportService.getAllReports();

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("processReport 메서드")
    class ProcessReportTest {

        @Test
        @DisplayName("존재하지 않는 신고를 처리하면 예외가 발생한다")
        void throwsException_whenReportNotFound() {
            // given
            given(reportRepository.findById(anyLong())).willReturn(Optional.empty());
            ReportProcessRequest request = new ReportProcessRequest(ReportProcessRequest.Action.REJECT);

            // when & then
            assertThatThrownBy(() -> adminReportService.processReport(999L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("이미 처리된 신고를 다시 처리하면 예외가 발생한다")
        void throwsException_whenReportAlreadyProcessed() {
            // given
            Report report = createReport(1L, ReportTargetType.ARTICLE, ReportStatus.ACCEPTED);
            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            ReportProcessRequest request = new ReportProcessRequest(ReportProcessRequest.Action.REJECT);

            // when & then
            assertThatThrownBy(() -> adminReportService.processReport(1L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("REJECT 액션으로 신고를 처리하면 상태가 REJECTED로 변경된다")
        void changesStatusToRejected_whenRejectAction() {
            // given
            Report report = createReport(1L, ReportTargetType.ARTICLE, ReportStatus.PENDING);
            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            ReportProcessRequest request = new ReportProcessRequest(ReportProcessRequest.Action.REJECT);

            // when
            adminReportService.processReport(1L, request);

            // then
            assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
            verify(adminContentService, never()).deleteArticle(anyLong());
            verify(adminContentService, never()).deleteComment(anyLong());
        }

        @Test
        @DisplayName("DELETE_CONTENT 액션으로 게시글 신고를 처리하면 게시글이 삭제되고 상태가 ACCEPTED로 변경된다")
        void deletesArticleAndChangesStatusToAccepted_whenDeleteContentActionForArticle() {
            // given
            Report report = createReport(1L, ReportTargetType.ARTICLE, ReportStatus.PENDING);
            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            ReportProcessRequest request = new ReportProcessRequest(ReportProcessRequest.Action.DELETE_CONTENT);

            // when
            adminReportService.processReport(1L, request);

            // then
            assertThat(report.getStatus()).isEqualTo(ReportStatus.ACCEPTED);
            verify(adminContentService).deleteArticle(100L);
            verify(adminContentService, never()).deleteComment(anyLong());
        }

        @Test
        @DisplayName("DELETE_CONTENT 액션으로 댓글 신고를 처리하면 댓글이 삭제되고 상태가 ACCEPTED로 변경된다")
        void deletesCommentAndChangesStatusToAccepted_whenDeleteContentActionForComment() {
            // given
            Report report = createReport(1L, ReportTargetType.COMMENT, ReportStatus.PENDING);
            given(reportRepository.findById(1L)).willReturn(Optional.of(report));
            ReportProcessRequest request = new ReportProcessRequest(ReportProcessRequest.Action.DELETE_CONTENT);

            // when
            adminReportService.processReport(1L, request);

            // then
            assertThat(report.getStatus()).isEqualTo(ReportStatus.ACCEPTED);
            verify(adminContentService).deleteComment(100L);
            verify(adminContentService, never()).deleteArticle(anyLong());
        }
    }
}
