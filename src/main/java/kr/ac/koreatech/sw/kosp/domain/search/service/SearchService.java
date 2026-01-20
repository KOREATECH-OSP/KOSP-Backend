package kr.ac.koreatech.sw.kosp.domain.search.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.challenge.repository.ChallengeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.repository.RecruitRepository;
import kr.ac.koreatech.sw.kosp.domain.community.team.repository.TeamRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubRepositoryRepository;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse.ArticleSummary;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse.ChallengeSummary;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse.OpenSourceSummary;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse.RecruitSummary;
import kr.ac.koreatech.sw.kosp.domain.search.dto.response.GlobalSearchResponse.TeamSummary;
import kr.ac.koreatech.sw.kosp.domain.search.model.SearchFilter;
import kr.ac.koreatech.sw.kosp.domain.search.model.SearchSortType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final ArticleRepository articleRepository;
    private final RecruitRepository recruitRepository;
    private final TeamRepository teamRepository;
    private final ChallengeRepository challengeRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;

    public GlobalSearchResponse search(String keyword) {
        return searchWithFilter(keyword, null, null);
    }

    public GlobalSearchResponse searchWithFilter(String keyword, Set<SearchFilter> filter, SearchSortType sort) {
        Set<SearchFilter> effectiveFilter = resolveFilter(filter);
        SearchSortType effectiveSort = resolveSort(sort);

        List<ArticleSummary> articles = searchArticles(keyword, effectiveFilter, effectiveSort);
        List<RecruitSummary> recruits = searchRecruits(keyword, effectiveFilter, effectiveSort);
        List<TeamSummary> teams = searchTeams(keyword, effectiveFilter);
        List<ChallengeSummary> challenges = searchChallenges(keyword, effectiveFilter);
        List<OpenSourceSummary> opensource = searchOpenSource(keyword, effectiveFilter);

        return new GlobalSearchResponse(articles, recruits, teams, challenges, opensource);
    }

    private Set<SearchFilter> resolveFilter(Set<SearchFilter> filter) {
        if (filter == null || filter.isEmpty()) {
            return Set.of(SearchFilter.values());
        }
        return filter;
    }

    private SearchSortType resolveSort(SearchSortType sort) {
        if (sort == null) {
            return SearchSortType.relevance;
        }
        return sort;
    }

    private List<ArticleSummary> searchArticles(String keyword, Set<SearchFilter> filter, SearchSortType sort) {
        if (!filter.contains(SearchFilter.articles)) {
            return Collections.emptyList();
        }

        List<ArticleSummary> results = articleRepository
            .findByTitleContainingAndIsDeletedFalse(keyword)
            .stream()
            .filter(article -> !(article instanceof Recruit))
            .map(ArticleSummary::from)
            .toList();

        return sortArticles(results, sort);
    }

    private List<ArticleSummary> sortArticles(List<ArticleSummary> articles, SearchSortType sort) {
        if (sort == SearchSortType.relevance) {
            return articles;
        }

        Comparator<ArticleSummary> comparator = Comparator.comparing(ArticleSummary::createdAt);
        if (sort == SearchSortType.date_desc) {
            comparator = comparator.reversed();
        }

        return articles.stream().sorted(comparator).toList();
    }

    private List<RecruitSummary> searchRecruits(String keyword, Set<SearchFilter> filter, SearchSortType sort) {
        if (!filter.contains(SearchFilter.recruits)) {
            return Collections.emptyList();
        }

        List<RecruitSummary> results = recruitRepository
            .findByTitleContainingAndIsDeletedFalse(keyword)
            .stream()
            .map(RecruitSummary::from)
            .toList();

        return sortRecruits(results, sort);
    }

    private List<RecruitSummary> sortRecruits(List<RecruitSummary> recruits, SearchSortType sort) {
        if (sort == SearchSortType.relevance) {
            return recruits;
        }

        Comparator<RecruitSummary> comparator = Comparator.comparing(RecruitSummary::createdAt);
        if (sort == SearchSortType.date_desc) {
            comparator = comparator.reversed();
        }

        return recruits.stream().sorted(comparator).toList();
    }

    private List<TeamSummary> searchTeams(String keyword, Set<SearchFilter> filter) {
        if (!filter.contains(SearchFilter.teams)) {
            return Collections.emptyList();
        }

        return teamRepository
            .findByNameContaining(keyword)
            .stream()
            .map(TeamSummary::from)
            .toList();
    }

    private List<ChallengeSummary> searchChallenges(String keyword, Set<SearchFilter> filter) {
        if (!filter.contains(SearchFilter.challenges)) {
            return Collections.emptyList();
        }

        return challengeRepository
            .findByNameContaining(keyword)
            .stream()
            .map(ChallengeSummary::from)
            .toList();
    }

    private List<OpenSourceSummary> searchOpenSource(String keyword, Set<SearchFilter> filter) {
        if (!filter.contains(SearchFilter.opensource)) {
            return Collections.emptyList();
        }

        List<GithubRepository> byName = githubRepositoryRepository.findByNameContainingIgnoreCase(keyword);
        List<GithubRepository> byDescription = githubRepositoryRepository.findByDescriptionContainingIgnoreCase(keyword);

        return Stream.concat(byName.stream(), byDescription.stream())
            .distinct()
            .map(OpenSourceSummary::from)
            .toList();
    }
}
