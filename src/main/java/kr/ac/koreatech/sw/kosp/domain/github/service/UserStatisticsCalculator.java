package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatisticsCalculator {

    private final GithubCommitDetailRawRepository commitDetailRawRepository;

    /**
     * MongoDB Raw 데이터로부터 사용자 통계 계산
     */
    public GithubUserStatistics calculate(String githubId) {
        log.info("Calculating statistics for user: {}", githubId);

        // MongoDB에서 사용자의 모든 커밋 조회
        List<GithubCommitDetailRaw> commits = commitDetailRawRepository.findByAuthorLogin(githubId);

        if (commits.isEmpty()) {
            log.warn("No commits found for user: {}", githubId);
            return createEmptyStatistics(githubId);
        }

        // 통계 계산
        GithubUserStatistics statistics = GithubUserStatistics.create(githubId);

        int totalCommits = commits.size();
        int totalAdditions = commits.stream()
            .mapToInt(GithubCommitDetailRaw::getAdditions)
            .sum();
        int totalDeletions = commits.stream()
            .mapToInt(GithubCommitDetailRaw::getDeletions)
            .sum();
        int totalLines = totalAdditions + totalDeletions;

        // 시간대 분석 (Night: 22:00-06:00, Day: 06:00-22:00)
        int nightCommits = (int) commits.stream()
            .filter(this::isNightCommit)
            .count();
        int dayCommits = totalCommits - nightCommits;

        // 레포지토리 수 계산
        long ownedReposCount = commits.stream()
            .map(c -> c.getRepoOwner() + "/" + c.getRepoName())
            .distinct()
            .count();

        // 데이터 기간 계산
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

        // 통계 업데이트
        statistics.updateStatistics(
            totalCommits,
            totalLines,
            totalAdditions,
            totalDeletions,
            0, // PRs (추후 구현)
            0, // Issues (추후 구현)
            (int) ownedReposCount,
            0, // Contributed repos (추후 구현)
            0, // Stars (추후 구현)
            nightCommits,
            dayCommits
        );

        // 데이터 기간 설정
        if (periodStart != null && periodEnd != null) {
            statistics.updateDataPeriod(periodStart, periodEnd);
        }

        // 점수 계산
        BigDecimal totalScore = calculateScore(statistics);
        statistics.updateScore(totalScore);

        log.info("Statistics calculated for {}: {} commits, {} lines, score: {}",
            githubId, totalCommits, totalLines, totalScore);

        return statistics;
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
     * 점수 계산
     * - 커밋: 10점
     * - 라인: 0.01점
     * - PR: 50점 (추후)
     * - 이슈: 30점 (추후)
     */
    private BigDecimal calculateScore(GithubUserStatistics stats) {
        BigDecimal commitScore = BigDecimal.valueOf(stats.getTotalCommits())
            .multiply(BigDecimal.valueOf(10));

        BigDecimal lineScore = BigDecimal.valueOf(stats.getTotalLines())
            .multiply(BigDecimal.valueOf(0.01));

        return commitScore.add(lineScore);
    }

    /**
     * 빈 통계 객체 생성
     */
    private GithubUserStatistics createEmptyStatistics(String githubId) {
        GithubUserStatistics statistics = GithubUserStatistics.create(githubId);
        statistics.updateScore(BigDecimal.ZERO);
        return statistics;
    }
}
