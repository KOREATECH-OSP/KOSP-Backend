package io.swkoreatech.kosp.domain.report.controller;

import io.swkoreatech.kosp.domain.report.dto.request.ReportRequest;
import io.swkoreatech.kosp.domain.report.service.ReportService;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/community")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/articles/{articleId}/reports")
    public ResponseEntity<Void> reportArticle(
        @AuthUser User user,
        @PathVariable Long articleId,
        @RequestBody @Valid ReportRequest request
    ) {
        reportService.reportArticle(user, articleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
