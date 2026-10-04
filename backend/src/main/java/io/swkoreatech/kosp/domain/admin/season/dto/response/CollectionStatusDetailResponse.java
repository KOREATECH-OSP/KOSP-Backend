package io.swkoreatech.kosp.domain.admin.season.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swkoreatech.kosp.domain.admin.season.dto.response.CollectionStatusResponse.CollectionStatus;

/**
 * 유저별 수집 현황 상세 응답 DTO.
 *
 * <p>레포지토리 단위로 커밋 수와 점수 반영 여부를 함께 반환한다.</p>
 */
public record CollectionStatusDetailResponse(
    Long userId,
    String userName,
    String githubLogin,
    LocalDateTime lastCrawling,
    CollectionStatus collectionStatus,
    long totalCommitCount,
    long scoredCommitCount,
    List<RepositoryCommitStat> repositories
) {
    /**
     * 레포지토리별 커밋 수집 통계.
     */
    public record RepositoryCommitStat(
        String repositoryName,
        long totalCommitCount,
        long scoredCommitCount
    ) {}
}
