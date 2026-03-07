package io.swkoreatech.kosp.domain.report.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.report.api.ReportApi;
import io.swkoreatech.kosp.domain.report.dto.request.ReportRequest;
import io.swkoreatech.kosp.domain.report.service.ReportService;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 신고 컨트롤러.
 * {@link ReportApi}의 구현체로, 신고 관련 요청을 처리한다.
 */
@RestController
@RequestMapping("/v1/community")
@RequiredArgsConstructor
public class ReportController implements ReportApi {

    private final ReportService reportService;

    /** {@inheritDoc} */
    @Override
    @PostMapping("/articles/{articleId}/reports")
    @Permit(name = "community:article:report", description = "게시글 신고")
    public ResponseEntity<Void> reportArticle(
        @AuthUser User user,
        @PathVariable Long articleId,
        @RequestBody @Valid ReportRequest request
    ) {
        reportService.reportArticle(user, articleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
