package io.swkoreatech.kosp.domain.admin.season.dto.response;

import java.time.LocalDateTime;

/**
 * 유저별 수집 현황 응답 DTO.
 */
public record CollectionStatusResponse(
    Long userId,
    String userName,
    String githubLogin,
    LocalDateTime lastCrawling,
    long totalCommitCount,
    CollectionStatus collectionStatus
) {
    public enum CollectionStatus {
        NORMAL,
        ANOMALY,
        NOT_COLLECTED
    }
}
