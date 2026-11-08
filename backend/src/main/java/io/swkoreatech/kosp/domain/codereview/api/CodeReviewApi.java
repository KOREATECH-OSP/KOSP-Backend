package io.swkoreatech.kosp.domain.codereview.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.codereview.dto.request.CreateCodeReviewRequest;
import io.swkoreatech.kosp.domain.codereview.dto.response.CodeReviewListResponse;
import io.swkoreatech.kosp.domain.codereview.dto.response.CodeReviewResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

@Tag(name = "CodeReview", description = "코드리뷰 API")
@RequestMapping("/v1/code-reviews")
public interface CodeReviewApi {

    @Operation(summary = "코드리뷰 목록 조회")
    @GetMapping
    ResponseEntity<CodeReviewListResponse> getReviews(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestParam String repoOwner,
        @RequestParam String repositoryName
    );

    @Operation(summary = "코드리뷰 작성")
    @PostMapping
    ResponseEntity<CodeReviewResponse> createReview(
        @Parameter(hidden = true) @AuthUser User user,
        @Valid @RequestBody CreateCodeReviewRequest request
    );

    @Operation(summary = "코드리뷰 삭제")
    @DeleteMapping("/{reviewId}")
    ResponseEntity<Void> deleteReview(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long reviewId
    );

    @Operation(summary = "코드리뷰 좋아요 토글")
    @PostMapping("/{reviewId}/like")
    ResponseEntity<Boolean> toggleLike(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long reviewId
    );
}
