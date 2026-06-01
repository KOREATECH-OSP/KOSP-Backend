package io.swkoreatech.kosp.domain.season.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.season.api.SeasonRankingApi;
import io.swkoreatech.kosp.domain.season.dto.response.MySeasonRankingResponse;
import io.swkoreatech.kosp.domain.season.dto.response.SeasonRankingListResponse;
import io.swkoreatech.kosp.domain.season.service.SeasonRankingService;
import lombok.RequiredArgsConstructor;

/**
 * 시즌 랭킹 조회 컨트롤러.
 * <p>{@link SeasonRankingApi}를 구현하여 시즌 랭킹 조회 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class SeasonRankingController implements SeasonRankingApi {

    private final SeasonRankingService seasonRankingService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<SeasonRankingListResponse> getCurrentRankings(Pageable pageable) {
        return ResponseEntity.ok(seasonRankingService.getRankings(pageable));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<SeasonRankingListResponse> getRankingsBySeason(Long seasonId, Pageable pageable) {
        return ResponseEntity.ok(seasonRankingService.getRankingsBySeason(seasonId, pageable));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MySeasonRankingResponse> getMyCurrentRanking(User user) {
        return ResponseEntity.ok(seasonRankingService.getMyRanking(user));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MySeasonRankingResponse> getMyRankingBySeason(Long seasonId, User user) {
        return ResponseEntity.ok(seasonRankingService.getMyRankingBySeason(seasonId, user));
    }
}
