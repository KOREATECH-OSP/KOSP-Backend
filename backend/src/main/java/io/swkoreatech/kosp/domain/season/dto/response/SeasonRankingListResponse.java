package io.swkoreatech.kosp.domain.season.dto.response;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonRankingScore;

/**
 * 시즌 랭킹 목록 응답 DTO.
 *
 * @param seasonName 시즌 이름
 * @param endDate    시즌 종료일
 * @param rankings   랭킹 목록
 * @param totalCount 전체 참여자 수
 * @param page       현재 페이지
 * @param size       페이지 크기
 */
public record SeasonRankingListResponse(
    String seasonName,
    LocalDate endDate,
    List<SeasonRankingEntryResponse> rankings,
    long totalCount,
    int page,
    int size
) {
    public static SeasonRankingListResponse of(Season season, Page<SeasonRankingScore> pageResult) {
        List<SeasonRankingEntryResponse> rankings = pageResult.getContent().stream()
            .map(SeasonRankingEntryResponse::from)
            .toList();

        return new SeasonRankingListResponse(
            season.getName(),
            season.getEndDate(),
            rankings,
            pageResult.getTotalElements(),
            pageResult.getNumber(),
            pageResult.getSize()
        );
    }
}
