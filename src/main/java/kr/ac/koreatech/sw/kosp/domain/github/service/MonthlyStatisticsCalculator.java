package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubMonthlyStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyStatisticsCalculator {

    private final GithubCommitDetailRawRepository commitDetailRawRepository;

    /**
     * 사용자의 월별 통계 계산
     */
    public List<GithubMonthlyStatistics> calculate(String githubId) {
        log.info("Calculating monthly statistics for user: {}", githubId);

        // MongoDB에서 사용자의 모든 커밋 조회
        List<GithubCommitDetailRaw> commits = commitDetailRawRepository.findByAuthorLogin(githubId);

        if (commits.isEmpty()) {
            log.warn("No commits found for user: {}", githubId);
            return List.of();
        }

        // 월별로 그룹화
        Map<YearMonth, List<GithubCommitDetailRaw>> commitsByMonth = commits.stream()
            .filter(commit -> commit.getAuthorDate() != null)
            .collect(Collectors.groupingBy(commit -> 
                YearMonth.from(commit.getAuthorDate())
            ));

        // 각 월별 통계 계산
        List<GithubMonthlyStatistics> monthlyStatistics = new ArrayList<>();

        for (Map.Entry<YearMonth, List<GithubCommitDetailRaw>> entry : commitsByMonth.entrySet()) {
            YearMonth yearMonth = entry.getKey();
            List<GithubCommitDetailRaw> monthCommits = entry.getValue();

            GithubMonthlyStatistics stats = calculateMonthStatistics(
                githubId,
                yearMonth.getYear(),
                yearMonth.getMonthValue(),
                monthCommits
            );

            monthlyStatistics.add(stats);
        }

        log.info("Calculated {} monthly statistics for user: {}", monthlyStatistics.size(), githubId);

        return monthlyStatistics;
    }

    /**
     * 특정 월의 통계 계산
     */
    private GithubMonthlyStatistics calculateMonthStatistics(
        String githubId,
        int year,
        int month,
        List<GithubCommitDetailRaw> commits
    ) {
        GithubMonthlyStatistics statistics = GithubMonthlyStatistics.create(githubId, year, month);

        int commitsCount = commits.size();
        int additionsCount = commits.stream()
            .mapToInt(GithubCommitDetailRaw::getAdditions)
            .sum();
        int deletionsCount = commits.stream()
            .mapToInt(GithubCommitDetailRaw::getDeletions)
            .sum();
        int linesCount = additionsCount + deletionsCount;

        // 레포지토리 수 계산
        long createdReposCount = commits.stream()
            .map(c -> c.getRepoOwner() + "/" + c.getRepoName())
            .distinct()
            .count();

        statistics.updateStatistics(
            commitsCount,
            linesCount,
            additionsCount,
            deletionsCount,
            (int) createdReposCount,
            0, // Contributed repos (추후 구현)
            0, // PRs (추후 구현)
            0  // Issues (추후 구현)
        );

        return statistics;
    }
}
