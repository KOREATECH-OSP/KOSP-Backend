package kr.ac.koreatech.sw.kosp.domain.search.service;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.challenge.model.Challenge;
import kr.ac.koreatech.sw.kosp.domain.challenge.repository.ChallengeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.repository.RecruitRepository;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.Team;
import kr.ac.koreatech.sw.kosp.domain.community.team.repository.TeamRepository;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse.ArticleSummary;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse.ChallengeSummary;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse.RecruitSummary;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse.TeamSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final ArticleRepository articleRepository;
    private final RecruitRepository recruitRepository;
    private final TeamRepository teamRepository;
    private final ChallengeRepository challengeRepository;

    public GlobalSearchResponse search(String keyword) {
        // 1. Articles (excluding Recruits to avoid duplication)
        List<ArticleSummary> articles = articleRepository
            .findByTitleContainingAndIsDeletedFalse(keyword)
            .stream()
            .filter(article -> !(article instanceof Recruit))
            .map(ArticleSummary::from)
            .toList();

        // 2. Recruits (separate query)
        List<RecruitSummary> recruits = recruitRepository
            .findByTitleContainingAndIsDeletedFalse(keyword)
            .stream()
            .map(RecruitSummary::from)
            .toList();

        // 3. Teams
        List<TeamSummary> teams = teamRepository
            .findByNameContaining(keyword)
            .stream()
            .map(TeamSummary::from)
            .toList();

        // 4. Challenges
        List<ChallengeSummary> challenges = challengeRepository
            .findByNameContaining(keyword)
            .stream()
            .map(ChallengeSummary::from)
            .toList();

        return new GlobalSearchResponse(articles, recruits, teams, challenges);
    }
}
