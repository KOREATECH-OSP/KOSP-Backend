package io.swkoreatech.kosp.domain.report.api;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.report.dto.request.ReportRequest;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Community - Report", description = "신고 API")
@RequestMapping("/v1/community")
public interface ReportApi {

    @Operation(summary = "게시글 신고", description = "게시글을 신고합니다.")
    @ApiResponse(responseCode = "201", description = "신고 성공")
    @PostMapping("/articles/{articleId}/reports")
    ResponseEntity<Void> reportArticle(
        @Parameter(hidden = true) @AuthUser User user,
        @Parameter(description = "게시글 ID") @PathVariable Long articleId,
        @RequestBody @Valid ReportRequest request
    );
}
