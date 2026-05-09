package io.swkoreatech.kosp.domain.github.dto.response;

import java.util.List;

/**
 * GitHub 기여 점수 기반 전체 랭킹 목록 응답 DTO.
 *
 * @param rankings   랭킹 목록
 * @param totalCount 전체 참여자 수
 * @param page       현재 페이지 (0-indexed)
 * @param size       페이지 크기
 */
public record GithubRankingListResponse(
    List<GithubRankingEntryResponse> rankings,
    long totalCount,
    int page,
    int size
) {
}
