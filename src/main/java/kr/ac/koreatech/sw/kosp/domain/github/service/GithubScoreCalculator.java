package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubScoreConfig;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubScoreConfigRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubScoreCalculator {

    private final GithubScoreConfigRepository scoreConfigRepository;
    private final GithubUserStatisticsRepository userStatisticsRepository;
    private final GithubRepositoryStatisticsRepository repoStatisticsRepository;

    public BigDecimal calculate(String githubId) {
        log.info("Calculating GitHub score for user: {}", githubId);

        // 1. 활성화된 점수 설정 조회
        GithubScoreConfig config = scoreConfigRepository
            .findByActive(true)
            .orElseGet(this::getDefaultConfig);

        log.debug("Using score config: {}", config.getConfigName());

        // 2. 사용자 통계 조회
        GithubUserStatistics userStats = userStatisticsRepository
            .findByGithubId(githubId)
            .orElseThrow(() -> new IllegalArgumentException("User statistics not found: " + githubId));

        // 3. 활동 수준 계산 (최대 3점)
        double activityLevel = calculateActivityLevel(userStats, config);
        log.debug("Activity level score: {}", activityLevel);

        // 4. 활동 다양성 계산 (최대 1점)
        double diversity = calculateDiversity(userStats, config);
        log.debug("Diversity score: {}", diversity);

        // 5. 활동 영향성 계산 (최대 5점)
        double impact = calculateImpact(githubId, config);
        log.debug("Impact score: {}", impact);

        // 6. 보너스 점수
        double bonus = calculateBonus(userStats, config);
        log.debug("Bonus score: {}", bonus);

        double totalScore = activityLevel + diversity + impact + bonus;

        log.info("Total score for {}: {} (activity: {}, diversity: {}, impact: {}, bonus: {})",
            githubId, totalScore, activityLevel, diversity, impact, bonus);

        return BigDecimal.valueOf(totalScore)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private double calculateActivityLevel(
        GithubUserStatistics stats,
        GithubScoreConfig config
    ) {
        // 커밋 점수 + 라인 점수
        double commitsScore = stats.getTotalCommits() * config.getCommitsWeight();
        double linesScore = stats.getTotalLines() * config.getLinesWeight();

        double rawScore = commitsScore + linesScore;

        // 최대 점수로 정규화
        return Math.min(rawScore, config.getActivityLevelMaxScore());
    }

    private double calculateDiversity(
        GithubUserStatistics stats,
        GithubScoreConfig config
    ) {
        int contributedRepos = stats.getContributedReposCount();
        int threshold = config.getDiversityRepoThreshold();

        // threshold 이상이면 만점
        double ratio = Math.min((double) contributedRepos / threshold, 1.0);
        return ratio * config.getDiversityMaxScore();
    }

    private double calculateImpact(String githubId, GithubScoreConfig config) {
        // 저장소별 통계에서 스타/포크 합산
        List<GithubRepositoryStatistics> repos =
            repoStatisticsRepository.findByContributorGithubId(githubId);

        if (repos.isEmpty()) {
            log.warn("No repository statistics found for user: {}", githubId);
            return 0.0;
        }

        int totalStars = repos.stream()
            .mapToInt(GithubRepositoryStatistics::getStargazersCount)
            .sum();

        int totalForks = repos.stream()
            .mapToInt(GithubRepositoryStatistics::getForksCount)
            .sum();

        double starsScore = totalStars * config.getStarsWeight();
        double forksScore = totalForks * config.getForksWeight();

        double rawScore = starsScore + forksScore;

        // 최대 점수로 정규화
        return Math.min(rawScore, config.getImpactMaxScore());
    }

    private double calculateBonus(
        GithubUserStatistics stats,
        GithubScoreConfig config
    ) {
        double bonus = 0.0;

        // Night Owl 보너스 (야간 커밋 비율이 50% 이상)
        if (stats.getTotalCommits() > 0) {
            double nightRatio = (double) stats.getNightCommits() / stats.getTotalCommits();
            if (nightRatio > 0.5) {
                bonus += config.getNightOwlBonus();
                log.debug("Night Owl bonus applied: {}", config.getNightOwlBonus());
            }
        }

        return bonus;
    }

    private GithubScoreConfig getDefaultConfig() {
        log.warn("No active score config found, using default");

        return GithubScoreConfig.builder()
            .configName("default")
            .active(true)
            .activityLevelMaxScore(3.0)
            .commitsWeight(0.01)
            .linesWeight(0.0001)
            .diversityMaxScore(1.0)
            .diversityRepoThreshold(10)
            .impactMaxScore(5.0)
            .starsWeight(0.01)
            .forksWeight(0.05)
            .contributorsWeight(0.02)
            .nightOwlBonus(0.5)
            .earlyAdopterBonus(0.3)
            .createdBy("system")
            .build();
    }
}
