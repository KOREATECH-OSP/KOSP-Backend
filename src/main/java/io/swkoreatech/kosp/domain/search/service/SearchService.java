package io.swkoreatech.kosp.domain.search.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.challenge.repository.ChallengeRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamRepository;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse.ArticleSummary;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse.ChallengeSummary;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse.RecruitSummary;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse.TeamSummary;
import io.swkoreatech.kosp.domain.search.model.SearchFilter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final ArticleRepository articleRepository;
    private final RecruitRepository recruitRepository;
    private final TeamRepository teamRepository;
    private final ChallengeRepository challengeRepository;

    public GlobalSearchResponse search(String keyword) {
        return search(keyword, null);
    }

    public GlobalSearchResponse search(String keyword, Set<SearchFilter> filters) {
        Set<SearchFilter> effectiveFilters = resolveFilters(filters);

        List<ArticleSummary> articles = searchArticles(keyword, effectiveFilters);
        List<RecruitSummary> recruits = searchRecruits(keyword, effectiveFilters);
        List<TeamSummary> teams = searchTeams(keyword, effectiveFilters);
        List<ChallengeSummary> challenges = searchChallenges(keyword, effectiveFilters);

        return new GlobalSearchResponse(articles, recruits, teams, challenges);
    }

    private Set<SearchFilter> resolveFilters(Set<SearchFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return Set.of(SearchFilter.values());
        }
        return filters;
    }

    private List<ArticleSummary> searchArticles(String keyword, Set<SearchFilter> filters) {
        if (!filters.contains(SearchFilter.articles)) {
            return Collections.emptyList();
        }
        return articleRepository.findByTitleContainingAndIsDeletedFalse(keyword)
            .stream()
            .filter(article -> !(article instanceof Recruit))
            .map(ArticleSummary::from)
            .toList();
    }

    private List<RecruitSummary> searchRecruits(String keyword, Set<SearchFilter> filters) {
        if (!filters.contains(SearchFilter.recruits)) {
            return Collections.emptyList();
        }
        return recruitRepository.findByTitleContainingAndIsDeletedFalse(keyword)
            .stream()
            .map(RecruitSummary::from)
            .toList();
    }

    private List<TeamSummary> searchTeams(String keyword, Set<SearchFilter> filters) {
        if (!filters.contains(SearchFilter.teams)) {
            return Collections.emptyList();
        }
        return teamRepository.findByNameContaining(keyword)
            .stream()
            .map(TeamSummary::from)
            .toList();
    }

    private List<ChallengeSummary> searchChallenges(String keyword, Set<SearchFilter> filters) {
        if (!filters.contains(SearchFilter.challenges)) {
            return Collections.emptyList();
        }
        return challengeRepository.findByNameContainingAndIsDeletedFalse(keyword)
            .stream()
            .map(ChallengeSummary::from)
            .toList();
    }
}
