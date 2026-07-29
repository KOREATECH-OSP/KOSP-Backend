package io.swkoreatech.kosp.domain.codereview.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.codereview.api.CodeReviewApi;
import io.swkoreatech.kosp.domain.codereview.dto.request.CreateCodeReviewRequest;
import io.swkoreatech.kosp.domain.codereview.dto.response.CodeReviewListResponse;
import io.swkoreatech.kosp.domain.codereview.dto.response.CodeReviewResponse;
import io.swkoreatech.kosp.domain.codereview.service.CodeReviewService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CodeReviewController implements CodeReviewApi {

    private final CodeReviewService codeReviewService;

    @Override
    @Permit(permitAll = true, description = "코드리뷰 목록 조회")
    public ResponseEntity<CodeReviewListResponse> getReviews(User user, String repoOwner, String repositoryName) {
        return ResponseEntity.ok(codeReviewService.getReviews(repoOwner, repositoryName, user));
    }

    @Override
    @Permit(description = "코드리뷰 작성")
    public ResponseEntity<CodeReviewResponse> createReview(User user, CreateCodeReviewRequest request) {
        return ResponseEntity.ok(codeReviewService.createReview(user, request));
    }

    @Override
    @Permit(description = "코드리뷰 삭제")
    public ResponseEntity<Void> deleteReview(User user, Long reviewId) {
        codeReviewService.deleteReview(user, reviewId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(description = "코드리뷰 좋아요")
    public ResponseEntity<Boolean> toggleLike(User user, Long reviewId) {
        return ResponseEntity.ok(codeReviewService.toggleLike(user, reviewId));
    }
}
