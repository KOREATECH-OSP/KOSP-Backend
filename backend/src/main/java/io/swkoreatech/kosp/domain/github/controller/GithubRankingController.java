package io.swkoreatech.kosp.domain.github.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.github.api.GithubRankingApi;
import io.swkoreatech.kosp.domain.github.dto.response.GithubRankingListResponse;
import io.swkoreatech.kosp.domain.github.dto.response.MyGithubRankingResponse;
import io.swkoreatech.kosp.domain.github.service.GithubRankingService;
import lombok.RequiredArgsConstructor;

/**
 * GitHub 기여 점수 기반 랭킹 컨트롤러.
 * {@link GithubRankingApi}를 구현하여 GitHub 랭킹 조회 기능을 제공한다.
 */
@RestController
@RequiredArgsConstructor
public class GithubRankingController implements GithubRankingApi {

    private final GithubRankingService githubRankingService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<GithubRankingListResponse> getRankings(Pageable pageable) {
        return ResponseEntity.ok(githubRankingService.getRankings(pageable));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MyGithubRankingResponse> getMyRanking(User user) {
        return ResponseEntity.ok(githubRankingService.getMyRanking(user));
    }
}
