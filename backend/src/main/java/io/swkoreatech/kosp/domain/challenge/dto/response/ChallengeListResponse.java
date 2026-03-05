package io.swkoreatech.kosp.domain.challenge.dto.response;

import java.util.List;

import io.swkoreatech.kosp.common.challenge.model.Challenge;
import io.swkoreatech.kosp.common.challenge.model.ImageResourceType;

/**
 * 도전 과제 목록 응답 DTO.
 *
 * @param challenges 도전 과제 목록
 * @param summary 도전 과제 요약 정보
 */
public record ChallengeListResponse(
    List<ChallengeResponse> challenges,
    ChallengeSummary summary
) {
    /**
     * 도전 과제 목록과 요약 정보로부터 응답 객체를 생성한다.
     *
     * @param challenges 도전 과제 응답 목록
     * @param summary 도전 과제 요약
     * @return 도전 과제 목록 응답
     */
    public static ChallengeListResponse from(List<ChallengeResponse> challenges, ChallengeSummary summary) {
        return new ChallengeListResponse(challenges, summary);
    }

    /**
     * 개별 도전 과제 응답 DTO.
     *
     * @param id 도전 과제 ID
     * @param title 도전 과제 제목
     * @param description 도전 과제 설명
     * @param category 카테고리
     * @param progress 진행률 (0~100)
     * @param isCompleted 달성 여부
     * @param imageResource 이미지 리소스 경로
     * @param imageResourceType 이미지 리소스 타입
     * @param tier 도전 과제 티어
     * @param point 보상 포인트
     */
    public record ChallengeResponse(
        Long id,
        String title,
        String description,
        String category,
        Integer progress,
        Boolean isCompleted,
        String imageResource,
        ImageResourceType imageResourceType,
        Integer tier,
        Integer point
    ) {
        /**
         * 도전 과제 엔티티와 진행 정보로부터 응답 객체를 생성한다.
         *
         * @param challenge 도전 과제 엔티티
         * @param progress 진행률
         * @param isCompleted 달성 여부
         * @return 도전 과제 응답
         */
        public static ChallengeResponse from(Challenge challenge, int progress, boolean isCompleted) {
            return new ChallengeResponse(
                challenge.getId(),
                challenge.getName(),
                challenge.getDescription(),
                "general",
                progress,
                isCompleted,
                challenge.getImageResource(),
                challenge.getImageResourceType(),
                challenge.getTier(),
                challenge.getPoint()
            );
        }
    }

    /**
     * 도전 과제 요약 정보 DTO.
     *
     * @param totalChallenges 전체 도전 과제 수
     * @param completedCount 달성 완료 수
     * @param overallProgress 전체 진행률
     * @param totalEarnedPoints 총 획득 포인트
     */
    public record ChallengeSummary(
        Long totalChallenges,
        Long completedCount,
        Double overallProgress,
        Integer totalEarnedPoints
    ) {
        /**
         * 요약 통계 정보로부터 응답 객체를 생성한다.
         *
         * @param totalChallenges 전체 도전 과제 수
         * @param completedCount 달성 완료 수
         * @param overallProgress 전체 진행률
         * @param totalEarnedPoints 총 획득 포인트
         * @return 도전 과제 요약 정보
         */
        public static ChallengeSummary from(
            long totalChallenges,
            long completedCount,
            double overallProgress,
            int totalEarnedPoints
        ) {
            return new ChallengeSummary(totalChallenges, completedCount, overallProgress, totalEarnedPoints);
        }
    }
}
