package io.swkoreatech.kosp.domain.search.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.domain.challenge.model.Challenge;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeRepository;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitRepository;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.repository.TeamRepository;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse.ArticleSummary;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse.ChallengeSummary;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse.RecruitSummary;
import io.swkoreatech.kosp.domain.search.dto.response.GlobalSearchResponse.TeamSummary;
import io.swkoreatech.kosp.domain.search.dto.response.UserSummary;
import io.swkoreatech.kosp.domain.search.model.SearchFilter;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.dto.PageMeta;
import io.swkoreatech.kosp.global.util.RsqlUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final ArticleRepository articleRepository;
    private final RecruitRepository recruitRepository;
    private final TeamRepository teamRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public GlobalSearchResponse search(String keyword) {
        return search(keyword, null, null, Pageable.unpaged());
    }

    public GlobalSearchResponse search(String keyword, Set<SearchFilter> filters) {
        return search(keyword, filters, null, Pageable.unpaged());
    }

    public GlobalSearchResponse search(String keyword, Set<SearchFilter> filters, String rsql, Pageable pageable) {
        Set<SearchFilter> effectiveFilters = resolveFilters(filters);

        List<ArticleSummary> articles = searchArticles(keyword, effectiveFilters, rsql, pageable);
        List<RecruitSummary> recruits = searchRecruits(keyword, effectiveFilters, rsql, pageable);
        List<TeamSummary> teams = searchTeams(keyword, effectiveFilters, rsql, pageable);
        List<ChallengeSummary> challenges = searchChallenges(keyword, effectiveFilters, rsql, pageable);
        List<UserSummary> users = searchUsers(keyword, effectiveFilters, rsql, pageable);

        PageMeta meta = createPageMeta(articles, recruits, teams, challenges, users);

        return new GlobalSearchResponse(articles, recruits, teams, challenges, users, meta);
    }

    private Set<SearchFilter> resolveFilters(Set<SearchFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return Set.of(SearchFilter.values());
        }
        return filters;
    }

    private List<ArticleSummary> searchArticles(String keyword, Set<SearchFilter> filters, String rsql, Pageable pageable) {
        if (!filters.contains(SearchFilter.articles)) {
            return Collections.emptyList();
        }

        Specification<Article> spec = createArticleSpec(keyword, rsql);
        Page<Article> page = articleRepository.findAll(spec, pageable);

        return page.getContent().stream()
            .filter(article -> !(article instanceof Recruit))
            .map(ArticleSummary::from)
            .toList();
    }

    private Specification<Article> createArticleSpec(String keyword, String rsql) {
        Specification<Article> baseSpec = createArticleBaseSpec();
        Specification<Article> keywordSpec = createArticleKeywordSpec(keyword);
        Specification<Article> rsqlSpec = RsqlUtils.toSpecification(rsql);

        return baseSpec.and(keywordSpec).and(rsqlSpec);
    }

    private Specification<Article> createArticleBaseSpec() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
    }

    private Specification<Article> createArticleKeywordSpec(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    private List<RecruitSummary> searchRecruits(String keyword, Set<SearchFilter> filters, String rsql, Pageable pageable) {
        if (!filters.contains(SearchFilter.recruits)) {
            return Collections.emptyList();
        }

        Specification<Recruit> spec = createRecruitSpec(keyword, rsql);
        Page<Recruit> page = recruitRepository.findAll(spec, pageable);

        return page.getContent().stream()
            .map(RecruitSummary::from)
            .toList();
    }

    private Specification<Recruit> createRecruitSpec(String keyword, String rsql) {
        Specification<Recruit> baseSpec = createRecruitBaseSpec();
        Specification<Recruit> keywordSpec = createRecruitKeywordSpec(keyword);
        Specification<Recruit> rsqlSpec = RsqlUtils.toSpecification(rsql);

        return baseSpec.and(keywordSpec).and(rsqlSpec);
    }

    private Specification<Recruit> createRecruitBaseSpec() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
    }

    private Specification<Recruit> createRecruitKeywordSpec(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    private List<TeamSummary> searchTeams(String keyword, Set<SearchFilter> filters, String rsql, Pageable pageable) {
        if (!filters.contains(SearchFilter.teams)) {
            return Collections.emptyList();
        }

        Specification<Team> spec = createTeamSpec(keyword, rsql);
        Page<Team> page = teamRepository.findAll(spec, pageable);

        return page.getContent().stream()
            .map(TeamSummary::from)
            .toList();
    }

    private Specification<Team> createTeamSpec(String keyword, String rsql) {
        Specification<Team> baseSpec = createTeamBaseSpec();
        Specification<Team> keywordSpec = createTeamKeywordSpec(keyword);
        Specification<Team> rsqlSpec = RsqlUtils.toSpecification(rsql);

        return baseSpec.and(keywordSpec).and(rsqlSpec);
    }

    private Specification<Team> createTeamBaseSpec() {
        return (root, query, cb) -> cb.conjunction();
    }

    private Specification<Team> createTeamKeywordSpec(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    private List<ChallengeSummary> searchChallenges(String keyword, Set<SearchFilter> filters, String rsql, Pageable pageable) {
        if (!filters.contains(SearchFilter.challenges)) {
            return Collections.emptyList();
        }

        Specification<Challenge> spec = createChallengeSpec(keyword, rsql);
        Page<Challenge> page = challengeRepository.findAll(spec, pageable);

        return page.getContent().stream()
            .map(ChallengeSummary::from)
            .toList();
    }

    private Specification<Challenge> createChallengeSpec(String keyword, String rsql) {
        Specification<Challenge> baseSpec = createChallengeBaseSpec();
        Specification<Challenge> keywordSpec = createChallengeKeywordSpec(keyword);
        Specification<Challenge> rsqlSpec = RsqlUtils.toSpecification(rsql);

        return baseSpec.and(keywordSpec).and(rsqlSpec);
    }

    private Specification<Challenge> createChallengeBaseSpec() {
        return (root, query, cb) -> cb.conjunction();
    }

    private Specification<Challenge> createChallengeKeywordSpec(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    private List<UserSummary> searchUsers(String keyword, Set<SearchFilter> filters, String rsql, Pageable pageable) {
        Specification<User> spec = createUserSpec(keyword, rsql);
        Page<User> page = userRepository.findAll(spec, pageable);

        return page.getContent().stream()
            .map(UserSummary::from)
            .toList();
    }

    private Specification<User> createUserSpec(String keyword, String rsql) {
        Specification<User> baseSpec = createUserBaseSpec();
        Specification<User> keywordSpec = createUserKeywordSpec(keyword);
        Specification<User> rsqlSpec = RsqlUtils.toSpecification(rsql);

        return baseSpec.and(keywordSpec).and(rsqlSpec);
    }

    private Specification<User> createUserBaseSpec() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
    }

    private Specification<User> createUserKeywordSpec(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }

        return createUserKeywordJoinSpec(keyword);
    }

    private Specification<User> createUserKeywordJoinSpec(String keyword) {
        return (root, query, cb) -> {
            Join<User, GithubUser> github = root.join("githubUser", JoinType.LEFT);
            String pattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(github.get("githubLogin")), pattern),
                cb.like(cb.lower(github.get("githubName")), pattern)
            );
        };
    }

    private PageMeta createPageMeta(List<ArticleSummary> articles, List<RecruitSummary> recruits, 
                                     List<TeamSummary> teams, List<ChallengeSummary> challenges, 
                                     List<UserSummary> users) {
        long totalItems = articles.size() + recruits.size() + teams.size() + challenges.size() + users.size();
        return new PageMeta(0, 1, totalItems);
    }
}
