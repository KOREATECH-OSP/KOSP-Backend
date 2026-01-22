package io.swkoreatech.kosp.domain.github.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "3. 기여내역 비교 항목")
public record GithubContributionComparisonResponse(
    Double avgCommitCount,
    Double avgStarCount,
    Double avgPrCount,
    Double avgIssueCount,
    
    Integer userCommitCount,
    Integer userStarCount,
    Integer userPrCount,
    Integer userIssueCount
) {}
