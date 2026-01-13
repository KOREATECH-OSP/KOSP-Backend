package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubYearlyStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubYearlyStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class YearlyStatisticsCalculator {

    private final GithubCommitDetailRawRepository commitDetailRawRepository;
    private final GithubYearlyStatisticsRepository yearlyStatisticsRepository;
    private final GithubPRIssueCountService prIssueCountService;

    /**
     * 특정 연도의 통계 계산
     */
    public GithubYearlyStatistics calculate(String githubId, int year) {
        log.info("Calculating yearly statistics for user: {} year: {}", githubId, year);

        // 해당 연도의 커밋 조회
        LocalDateTime yearStart = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime yearEnd = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        List<GithubCommitDetailRaw> yearCommits = commitDetailRawRepository
            .findByAuthorLogin(githubId)
            .stream()
            .filter(c -> c.getAuthorDate() != null)
            .filter(c -> !c.getAuthorDate().isBefore(yearStart) && !c.getAuthorDate().isAfter(yearEnd))
            .toList();

        GithubYearlyStatistics statistics = GithubYearlyStatistics.create(githubId, year);

        if (yearCommits.isEmpty()) {
            log.warn("No commits found for user: {} in year: {}", githubId, year);
            return statistics;
        }

        // 기본 통계 계산
        int commits = yearCommits.size();
        int additions = yearCommits.stream().mapToInt(GithubCommitDetailRaw::getAdditions).sum();
        int deletions = yearCommits.stream().mapToInt(GithubCommitDetailRaw::getDeletions).sum();
        int lines = additions + deletions;

        statistics.updateStatistics(commits, lines, additions, deletions, 0, 0);

        // PR/Issue 통계 (연도별)
        int yearlyPrs = prIssueCountService.countUserPRsByYear(githubId, year);
        int yearlyIssues = prIssueCountService.countUserIssuesByYear(githubId, year);
        
        statistics.updateStatistics(commits, lines, additions, deletions, yearlyPrs, yearlyIssues);

        // 점수 계산 (간단 버전 - 메인 저장소 구분 없이)
        BigDecimal commitScore = BigDecimal.valueOf(commits * 10);
        BigDecimal lineScore = BigDecimal.valueOf(lines).multiply(BigDecimal.valueOf(0.01));
        BigDecimal totalScore = commitScore.add(lineScore);

        statistics.updateScores(totalScore, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        // 최고 저장소 찾기
        Map<String, Long> commitsByRepo = yearCommits.stream()
            .collect(Collectors.groupingBy(
                c -> c.getRepoOwner() + "/" + c.getRepoName(),
                Collectors.counting()
            ));

        if (!commitsByRepo.isEmpty()) {
            Map.Entry<String, Long> bestRepo = commitsByRepo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

            if (bestRepo != null) {
                String[] parts = bestRepo.getKey().split("/");
                if (parts.length == 2) {
                    statistics.updateBestRepository(parts[0], parts[1], bestRepo.getValue().intValue());
                }
            }
        }

        log.info("Yearly statistics calculated for {} ({}): {} commits, {} lines",
            githubId, year, commits, lines);

        return statistics;
    }

    /**
     * 모든 연도의 통계 계산
     */
    public List<GithubYearlyStatistics> calculateAll(String githubId) {
        log.info("Calculating all yearly statistics for user: {}", githubId);

        List<GithubCommitDetailRaw> allCommits = commitDetailRawRepository.findByAuthorLogin(githubId);

        if (allCommits.isEmpty()) {
            log.warn("No commits found for user: {}", githubId);
            return List.of();
        }

        // 커밋이 있는 연도 추출
        List<Integer> years = allCommits.stream()
            .map(GithubCommitDetailRaw::getAuthorDate)
            .filter(date -> date != null)
            .map(LocalDateTime::getYear)
            .distinct()
            .sorted()
            .toList();

        // 각 연도별 통계 계산
        return years.stream()
            .map(year -> calculate(githubId, year))
            .toList();
    }
}
