package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatisticsCalculator {

    private final GithubCommitDetailRawRepository commitDetailRawRepository;
    private final GithubRepositoryStatisticsRepository repositoryStatisticsRepository;
    private final GithubPRIssueCountService prIssueCountService;

    private static final int MAX_MAIN_REPOS = 3;

    /**
     * MongoDB Raw 데이터로부터 사용자 통계 계산
     */
    public GithubUserStatistics calculate(String githubId) {
        log.info("Calculating detailed statistics for user: {}", githubId);

        // MongoDB에서 사용자의 모든 커밋 조회
        List<GithubCommitDetailRaw> commits = commitDetailRawRepository.findByAuthorLogin(githubId);

        if (commits.isEmpty()) {
            log.warn("No commits found for user: {}", githubId);
            return createEmptyStatistics(githubId);
        }

        // 통계 계산
        GithubUserStatistics statistics = GithubUserStatistics.create(githubId);

        // 1. 메인 저장소 선정 (커밋 수 기준 상위 3개)
        Set<String> mainRepositories = selectMainRepositories(commits);
        log.info("Selected {} main repositories for user: {}", mainRepositories.size(), githubId);

        // 2. 메인 vs 다른 저장소 통계 분리
        Map<Boolean, List<GithubCommitDetailRaw>> partitionedCommits = commits.stream()
            .collect(Collectors.partitioningBy(c -> 
                mainRepositories.contains(c.getRepoOwner() + "/" + c.getRepoName())
            ));

        List<GithubCommitDetailRaw> mainRepoCommits = partitionedCommits.get(true);
        List<GithubCommitDetailRaw> otherRepoCommits = partitionedCommits.get(false);

        // 3. 기본 통계 계산
        int totalCommits = commits.size();
        int totalAdditions = commits.stream().mapToInt(GithubCommitDetailRaw::getAdditions).sum();
        int totalDeletions = commits.stream().mapToInt(GithubCommitDetailRaw::getDeletions).sum();
        int totalLines = totalAdditions + totalDeletions;

        int mainRepoCommitsCount = mainRepoCommits.size();
        int mainRepoAdditions = mainRepoCommits.stream().mapToInt(GithubCommitDetailRaw::getAdditions).sum();
        int mainRepoDeletions = mainRepoCommits.stream().mapToInt(GithubCommitDetailRaw::getDeletions).sum();
        int mainRepoLines = mainRepoAdditions + mainRepoDeletions;

        int otherRepoCommitsCount = otherRepoCommits.size();
        int otherRepoAdditions = otherRepoCommits.stream().mapToInt(GithubCommitDetailRaw::getAdditions).sum();
        int otherRepoDeletions = otherRepoCommits.stream().mapToInt(GithubCommitDetailRaw::getDeletions).sum();
        int otherRepoLines = otherRepoAdditions + otherRepoDeletions;

        // 4. 시간대 분석 (Night: 22:00-06:00, Day: 06:00-22:00)
        int nightCommits = (int) commits.stream().filter(this::isNightCommit).count();
        int dayCommits = totalCommits - nightCommits;

        // 5. 저장소 통계 조회 (먼저 조회)
        List<GithubRepositoryStatistics> repoStats = repositoryStatisticsRepository
            .findByContributorGithubId(githubId);

        // 6. 레포지토리 수 계산
        // contributedRepos: 소유하지 않은 저장소 (repoOwner != githubId)
        long contributedReposCount = repoStats.stream()
            .filter(r -> !r.getRepoOwner().equalsIgnoreCase(githubId))
            .map(r -> r.getRepoOwner() + "/" + r.getRepoName())
            .distinct()
            .count();

        // ownedRepos: 소유한 저장소 (repoOwner == githubId)
        long ownedReposCount = repoStats.stream()
            .filter(r -> r.getRepoOwner().equalsIgnoreCase(githubId))
            .map(r -> r.getRepoOwner() + "/" + r.getRepoName())
            .distinct()
            .count();

        // 7. PR/Issue 통계
        int totalPrs = prIssueCountService.countUserPRs(githubId);
        int totalIssues = prIssueCountService.countUserIssues(githubId);

        // 8. 평판 통계 (Stars, Forks)
        int totalStarsReceived = repoStats.stream()
            .mapToInt(r -> r.getStargazersCount() != null ? r.getStargazersCount() : 0)
            .sum();
        
        int totalForksReceived = repoStats.stream()
            .mapToInt(r -> r.getForksCount() != null ? r.getForksCount() : 0)
            .sum();

        // 9. 데이터 기간 계산
        LocalDate periodStart = commits.stream()
            .map(GithubCommitDetailRaw::getAuthorDate)
            .filter(date -> date != null)
            .map(LocalDateTime::toLocalDate)
            .min(LocalDate::compareTo)
            .orElse(null);

        LocalDate periodEnd = commits.stream()
            .map(GithubCommitDetailRaw::getAuthorDate)
            .filter(date -> date != null)
            .map(LocalDateTime::toLocalDate)
            .max(LocalDate::compareTo)
            .orElse(null);

        // 9. 통계 업데이트
        statistics.updateStatistics(
            totalCommits,
            totalLines,
            totalAdditions,
            totalDeletions,
            totalPrs,
            totalIssues,
            (int) ownedReposCount,
            (int) contributedReposCount,
            totalStarsReceived,
            totalForksReceived,
            nightCommits,
            dayCommits
        );

        // 10. 데이터 기간 설정
        if (periodStart != null && periodEnd != null) {
            statistics.updateDataPeriod(periodStart, periodEnd);
        }

        // 11. 점수 계산
        BigDecimal mainRepoScore = calculateMainRepoScore(mainRepoCommitsCount, mainRepoLines);
        BigDecimal otherRepoScore = calculateOtherRepoScore(otherRepoCommitsCount, otherRepoLines);
        BigDecimal prIssueScore = calculatePRIssueScore(totalPrs, totalIssues);
        BigDecimal reputationScore = calculateReputationScore(totalStarsReceived, totalForksReceived);

        statistics.updateDetailedScore(mainRepoScore, otherRepoScore, prIssueScore, reputationScore);

        log.info("Statistics calculated for {}: {} commits, {} lines, total score: {}, " +
            "main repo: {}, other repo: {}, PR/Issue: {}, reputation: {}",
            githubId, totalCommits, totalLines, 
            mainRepoScore.add(otherRepoScore).add(prIssueScore).add(reputationScore),
            mainRepoScore, otherRepoScore, prIssueScore, reputationScore);

        return statistics;
    }

    /**
     * 메인 저장소 선정 (커밋 수 기준 상위 3개)
     */
    private Set<String> selectMainRepositories(List<GithubCommitDetailRaw> commits) {
        Map<String, Long> commitsByRepo = commits.stream()
            .collect(Collectors.groupingBy(
                c -> c.getRepoOwner() + "/" + c.getRepoName(),
                Collectors.counting()
            ));

        return commitsByRepo.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(MAX_MAIN_REPOS)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    /**
     * 메인 저장소 점수 계산
     * - 커밋: 15점
     * - 라인: 0.02점
     */
    private BigDecimal calculateMainRepoScore(int commits, int lines) {
        BigDecimal commitScore = BigDecimal.valueOf(commits).multiply(BigDecimal.valueOf(15));
        BigDecimal lineScore = BigDecimal.valueOf(lines).multiply(BigDecimal.valueOf(0.02));
        return commitScore.add(lineScore);
    }

    /**
     * 다른 저장소 점수 계산
     * - 커밋: 5점
     * - 라인: 0.01점
     */
    private BigDecimal calculateOtherRepoScore(int commits, int lines) {
        BigDecimal commitScore = BigDecimal.valueOf(commits).multiply(BigDecimal.valueOf(5));
        BigDecimal lineScore = BigDecimal.valueOf(lines).multiply(BigDecimal.valueOf(0.01));
        return commitScore.add(lineScore);
    }

    /**
     * PR/Issue 점수 계산
     * - PR: 50점
     * - Issue: 30점
     */
    private BigDecimal calculatePRIssueScore(int prs, int issues) {
        return BigDecimal.valueOf(prs * 50 + issues * 30);
    }

    /**
     * 평판 점수 계산
     * - Star: 10점
     * - Fork: 20점
     */
    private BigDecimal calculateReputationScore(int stars, int forks) {
        return BigDecimal.valueOf(stars * 10 + forks * 20);
    }

    /**
     * 야간 커밋 여부 판단 (22:00-06:00)
     */
    private boolean isNightCommit(GithubCommitDetailRaw commit) {
        LocalDateTime authorDate = commit.getAuthorDate();
        if (authorDate == null) {
            return false;
        }

        int hour = authorDate.getHour();
        return hour >= 22 || hour < 6;
    }

    /**
     * 빈 통계 객체 생성
     */
    private GithubUserStatistics createEmptyStatistics(String githubId) {
        GithubUserStatistics statistics = GithubUserStatistics.create(githubId);
        statistics.updateDetailedScore(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );
        return statistics;
    }
}
