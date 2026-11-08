package io.swkoreatech.kosp.domain.codereview.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.codereview.model.CodeReview;
import io.swkoreatech.kosp.common.codereview.model.CodeReviewLike;
import io.swkoreatech.kosp.common.codereview.repository.CodeReviewLikeRepository;
import io.swkoreatech.kosp.common.codereview.repository.CodeReviewRepository;
import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.codereview.dto.request.CreateCodeReviewRequest;
import io.swkoreatech.kosp.domain.codereview.dto.response.CodeReviewListResponse;
import io.swkoreatech.kosp.domain.codereview.dto.response.CodeReviewResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodeReviewService {

    private final CodeReviewRepository codeReviewRepository;
    private final CodeReviewLikeRepository codeReviewLikeRepository;

    public CodeReviewListResponse getReviews(String repoOwner, String repositoryName, User me) {
        List<CodeReview> reviews = codeReviewRepository
            .findByRepoOwnerAndRepositoryNameAndParentIdIsNullOrderByCreatedAtDesc(repoOwner, repositoryName)
            .stream()
            .filter(review -> canViewReview(review, me, repoOwner))
            .toList();

        List<Long> reviewIds = reviews.stream().map(CodeReview::getId).toList();
        Set<Long> likedIds = me != null
            ? codeReviewLikeRepository.findLikedReviewIds(me.getId(), reviewIds)
            : Collections.emptySet();

        // 댓글(replies) 조회
        Map<Long, List<CodeReview>> repliesMap = reviews.stream()
            .collect(Collectors.toMap(CodeReview::getId, r ->
                codeReviewRepository.findByParentIdOrderByCreatedAtAsc(r.getId())));

        List<CodeReviewResponse> responses = reviews.stream().map(review -> {
            List<CodeReview> replyEntities = repliesMap.getOrDefault(review.getId(), List.of());
            List<Long> replyIds = replyEntities.stream().map(CodeReview::getId).toList();
            Set<Long> replyLikedIds = me != null
                ? codeReviewLikeRepository.findLikedReviewIds(me.getId(), replyIds)
                : Collections.emptySet();
            List<CodeReviewResponse> replyResponses = replyEntities.stream()
                .map(r -> CodeReviewResponse.of(r, replyLikedIds.contains(r.getId()), List.of()))
                .toList();
            return CodeReviewResponse.of(review, likedIds.contains(review.getId()), replyResponses);
        }).toList();

        return new CodeReviewListResponse(reviews.size(), responses);
    }

    private boolean canViewReview(CodeReview review, User me, String repoOwner) {
        if (!review.isPrivate()) return true;
        if (me == null) return false;
        if (me.getId().equals(review.getUser().getId())) return true;
        return me.getGithubUser() != null && repoOwner.equals(me.getGithubUser().getGithubLogin());
    }

    @Transactional
    public CodeReviewResponse createReview(User me, CreateCodeReviewRequest request) {
        CodeReview review = CodeReview.builder()
            .repoOwner(request.repoOwner())
            .repositoryName(request.repositoryName())
            .user(me)
            .content(request.content())
            .parentId(request.parentId())
            .isPrivate(request.isPrivate())
            .build();
        codeReviewRepository.save(review);
        return CodeReviewResponse.of(review, false, List.of());
    }

    @Transactional
    public void deleteReview(User me, Long reviewId) {
        CodeReview review = codeReviewRepository.getById(reviewId);
        if (!review.getUser().getId().equals(me.getId())) {
            throw new GlobalException(ExceptionMessage.CODE_REVIEW_NOT_OWNER);
        }
        codeReviewRepository.delete(review);
    }

    @Transactional
    public boolean toggleLike(User me, Long reviewId) {
        CodeReview review = codeReviewRepository.getById(reviewId);
        return codeReviewLikeRepository.findByUserIdAndCodeReviewId(me.getId(), reviewId)
            .map(like -> {
                codeReviewLikeRepository.delete(like);
                review.decrementLikes();
                return false;
            })
            .orElseGet(() -> {
                codeReviewLikeRepository.save(CodeReviewLike.builder().user(me).codeReview(review).build());
                review.incrementLikes();
                return true;
            });
    }
}
