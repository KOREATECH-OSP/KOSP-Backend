package io.swkoreatech.kosp.domain.github.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.common.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.github.dto.response.GithubRankingEntryResponse;
import io.swkoreatech.kosp.domain.github.dto.response.GithubRankingListResponse;
import lombok.RequiredArgsConstructor;

/**
 * GitHub 기여 점수 기반 랭킹 서비스.
 * 전체 기간 크롤링 기반 GitHub 점수로 산정된 랭킹을 조회한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GithubRankingService {

    private final GithubUserStatisticsRepository statisticsRepository;
    private final UserRepository userRepository;

    /**
     * GitHub 기여 점수 기반 전체 랭킹을 조회한다.
     *
     * @param pageable 페이징 정보
     * @return 랭킹 목록 응답
     */
    public GithubRankingListResponse getRankings(Pageable pageable) {
        Page<GithubUserStatistics> statsPage = statisticsRepository.findAllOrderByTotalScoreDesc(pageable);

        // githubId(String) → Long 변환 후 배치 조회
        List<Long> githubIds = statsPage.getContent().stream()
            .map(gs -> Long.parseLong(gs.getGithubId()))
            .toList();

        Map<Long, User> userByGithubId = userRepository.findAllByGithubIds(githubIds).stream()
            .collect(Collectors.toMap(
                u -> u.getGithubUser().getGithubId(),
                u -> u
            ));

        int baseRank = (int) pageable.getOffset() + 1;
        List<GithubRankingEntryResponse> rankings = new ArrayList<>();

        for (int i = 0; i < statsPage.getContent().size(); i++) {
            GithubUserStatistics stats = statsPage.getContent().get(i);
            Long githubId = Long.parseLong(stats.getGithubId());
            User user = userByGithubId.get(githubId);

            if (user == null) {
                continue; // GitHub 연동 해제 또는 탈퇴 유저 제외
            }

            rankings.add(GithubRankingEntryResponse.of(baseRank + i, user, stats));
        }

        return new GithubRankingListResponse(
            rankings,
            statsPage.getTotalElements(),
            pageable.getPageNumber(),
            pageable.getPageSize()
        );
    }
}
