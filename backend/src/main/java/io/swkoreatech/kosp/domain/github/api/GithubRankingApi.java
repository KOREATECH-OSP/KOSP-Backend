package io.swkoreatech.kosp.domain.github.api;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.github.dto.response.GithubRankingListResponse;
import io.swkoreatech.kosp.domain.github.dto.response.MyGithubRankingResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

/**
 * GitHub 기여 점수 기반 랭킹 API.
 * 전체 기간 크롤링 기반 GitHub 점수로 산정된 전체 랭킹 조회 기능을 정의한다.
 */
@Tag(name = "GitHub 랭킹", description = "GitHub 기여 점수 기반 랭킹 API")
@RequestMapping("/v1/github")
public interface GithubRankingApi {

    /**
     * GitHub 기여 점수 기반 전체 랭킹을 조회한다.
     *
     * @param pageable 페이징 정보 (기본: 50건)
     * @return 랭킹 목록 응답
     */
    @Operation(
        summary = "GitHub 기여 점수 전체 랭킹 조회",
        description = "전체 기간 GitHub 활동·다양성·영향력 점수 기반으로 산정된 전체 랭킹을 조회합니다."
    )
    @GetMapping("/rankings")
    ResponseEntity<GithubRankingListResponse> getRankings(
        @PageableDefault(size = 50) Pageable pageable
    );

    /**
     * 로그인한 사용자의 GitHub 기여 점수 기반 랭킹을 조회한다.
     */
    @Operation(summary = "내 GitHub 기여 점수 랭킹 조회", description = "내 전체 기간 GitHub 활동·다양성·영향력 점수 기반 순위를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "404", description = "GitHub 연동 없음 또는 통계 없음")
    @GetMapping("/rankings/me")
    ResponseEntity<MyGithubRankingResponse> getMyRanking(@AuthUser User user);
}
