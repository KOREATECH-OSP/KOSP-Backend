package io.swkoreatech.kosp.domain.admin.season.dto.response;

/**
 * 유저별 수집 현황 응답 DTO.
 */
public record CollectionStatusResponse(
    Long userId,
    String userName,
    String githubLogin,
    String lastCrawling,
    long totalCommitCount,
    CollectionStatus collectionStatus
) {
    public enum CollectionStatus {
        COLLECTING,
        NORMAL,
        ANOMALY,
        FAILED,
        NOT_COLLECTED
    }
}
