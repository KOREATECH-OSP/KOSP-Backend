package io.swkoreatech.kosp.domain.season.api;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.season.dto.response.MySeasonRankingResponse;
import io.swkoreatech.kosp.domain.season.dto.response.SeasonRankingListResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

/**
 * 시즌 랭킹 조회 API 인터페이스.
 */
@Tag(name = "Season Ranking", description = "시즌 랭킹 조회 API")
@RequestMapping("/v1/seasons")
public interface SeasonRankingApi {

    /**
     * 현재 활성 시즌의 전체 랭킹을 조회한다 (비로그인 공개).
     */
    @Operation(summary = "현재 시즌 전체 랭킹 조회", description = "현재 활성 시즌의 전체 랭킹을 조회합니다. 비로그인 상태에서도 조회 가능합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "활성 시즌 없음")
    @GetMapping("/current/rankings")
    ResponseEntity<SeasonRankingListResponse> getCurrentRankings(
        @PageableDefault(size = 50) Pageable pageable
    );

    /**
     * 특정 시즌의 전체 랭킹을 조회한다 (비로그인 공개).
     */
    @Operation(summary = "특정 시즌 전체 랭킹 조회", description = "특정 시즌의 전체 랭킹을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "시즌을 찾을 수 없음")
    @GetMapping("/{seasonId}/rankings")
    ResponseEntity<SeasonRankingListResponse> getRankingsBySeason(
        @PathVariable Long seasonId,
        @PageableDefault(size = 50) Pageable pageable
    );

    /**
     * 현재 활성 시즌에서 내 랭킹을 조회한다 (로그인 필요).
     */
    @Operation(summary = "현재 시즌 내 랭킹 조회", description = "현재 활성 시즌에서 내 점수와 순위를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "404", description = "활성 시즌 없음 또는 랭킹 없음")
    @GetMapping("/current/rankings/me")
    ResponseEntity<MySeasonRankingResponse> getMyCurrentRanking(@AuthUser User user);

    /**
     * 특정 시즌에서 내 랭킹을 조회한다 (로그인 필요).
     */
    @Operation(summary = "특정 시즌 내 랭킹 조회", description = "특정 시즌에서 내 점수와 순위를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{seasonId}/rankings/me")
    ResponseEntity<MySeasonRankingResponse> getMyRankingBySeason(
        @PathVariable Long seasonId,
        @AuthUser User user
    );
}
