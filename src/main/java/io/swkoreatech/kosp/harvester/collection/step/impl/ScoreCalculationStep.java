package io.swkoreatech.kosp.harvester.collection.step.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.harvester.collection.document.CommitDocument;
import io.swkoreatech.kosp.harvester.collection.document.ContributedRepoDocument;
import io.swkoreatech.kosp.harvester.collection.document.PullRequestDocument;
import io.swkoreatech.kosp.harvester.collection.repository.CommitDocumentRepository;
import io.swkoreatech.kosp.harvester.collection.repository.ContributedRepoDocumentRepository;
import io.swkoreatech.kosp.harvester.collection.repository.PullRequestDocumentRepository;
import io.swkoreatech.kosp.harvester.collection.step.StepProvider;
import io.swkoreatech.kosp.harvester.job.StepCompletionListener;
import io.swkoreatech.kosp.harvester.statistics.model.GithubUserStatistics;
import io.swkoreatech.kosp.harvester.statistics.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.harvester.trigger.ChallengeCheckPublisher;
import io.swkoreatech.kosp.harvester.user.GithubUser;
import io.swkoreatech.kosp.harvester.user.User;
import io.swkoreatech.kosp.harvester.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreCalculationStep implements StepProvider {

    private static final String STEP_NAME = "scoreCalculationStep";

    private static final int STAR_THRESHOLD_FOR_IMPACT = 1000;
    private static final int STAR_THRESHOLD_FOR_OWNED_REPO = 100;
    private static final int CLOSED_ISSUES_THRESHOLD = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final CommitDocumentRepository commitRepository;
    private final PullRequestDocumentRepository prRepository;
    private final ContributedRepoDocumentRepository repoRepository;
    private final GithubUserStatisticsRepository statisticsRepository;
    private final ChallengeCheckPublisher challengeCheckPublisher;
    private final StepCompletionListener stepCompletionListener;

    @Override
    public Step getStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet((contribution, chunkContext) -> {
                Long userId = extractUserId(chunkContext);
                execute(userId);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .listener(stepCompletionListener)
            .build();
    }

    @Override
    public String getStepName() {
        return STEP_NAME;
    }

    private Long extractUserId(ChunkContext chunkContext) {
        return chunkContext.getStepContext()
            .getStepExecution()
            .getJobParameters()
            .getLong("userId");
    }

    private void execute(Long userId) {
        User user = userRepository.getById(userId);
        if (!user.hasGithubUser()) {
            log.warn("User {} does not have GitHub account linked", userId);
            return;
        }

        GithubUser githubUser = user.getGithubUser();
        String githubId = String.valueOf(githubUser.getGithubId());

        BigDecimal activityScore = calculateActivityScore(userId);
        BigDecimal diversityScore = calculateDiversityScore(userId);
        BigDecimal impactScore = calculateImpactScore(userId);

        saveScores(githubId, activityScore, diversityScore, impactScore);
        challengeCheckPublisher.publish(userId, githubId);

        log.info("Calculated scores for user {}: activity={}, diversity={}, impact={}",
            userId, activityScore, diversityScore, impactScore);
    }

    private BigDecimal calculateActivityScore(Long userId) {
        List<CommitDocument> commits = commitRepository.findByUserId(userId);
        List<PullRequestDocument> prs = prRepository.findByUserId(userId);

        Map<String, RepoStats> repoStatsMap = new HashMap<>();

        for (CommitDocument commit : commits) {
            String repoKey = buildRepoKey(commit.getRepositoryOwner(), commit.getRepositoryName());
            repoStatsMap.computeIfAbsent(repoKey, k -> new RepoStats()).commitCount++;
        }

        for (PullRequestDocument pr : prs) {
            String repoKey = buildRepoKey(pr.getRepositoryOwner(), pr.getRepositoryName());
            repoStatsMap.computeIfAbsent(repoKey, k -> new RepoStats()).prCount++;
        }

        int maxScore = 0;
        for (RepoStats stats : repoStatsMap.values()) {
            int score = calculateRepoActivityScore(stats.commitCount, stats.prCount);
            maxScore = Math.max(maxScore, score);
        }

        return BigDecimal.valueOf(maxScore);
    }

    private String buildRepoKey(String owner, String name) {
        return owner + "/" + name;
    }

    private int calculateRepoActivityScore(int commitCount, int prCount) {
        if (commitCount >= 100 && prCount >= 20) {
            return 3;
        }
        if (commitCount >= 30 && prCount >= 5) {
            return 2;
        }
        if (commitCount >= 5 || prCount >= 1) {
            return 1;
        }
        return 0;
    }

    private BigDecimal calculateDiversityScore(Long userId) {
        List<ContributedRepoDocument> repos = repoRepository.findByUserId(userId);
        int repoCount = repos.size();

        if (repoCount >= 10) {
            return BigDecimal.valueOf(1.0);
        }
        if (repoCount >= 5) {
            return BigDecimal.valueOf(0.7);
        }
        if (repoCount >= 2) {
            return BigDecimal.valueOf(0.4);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateImpactScore(Long userId) {
        List<ContributedRepoDocument> repos = repoRepository.findByUserId(userId);
        List<PullRequestDocument> prs = prRepository.findByUserId(userId);

        BigDecimal score = BigDecimal.ZERO;

        score = score.add(calculateOwnedRepoStarBonus(repos));
        score = score.add(calculateHighStarPrBonus(prs));
        score = score.add(calculateClosedIssuesBonus(prs));
        score = score.add(calculateCrossRepoPrBonus(prs));

        return score.min(BigDecimal.valueOf(5));
    }

    private BigDecimal calculateOwnedRepoStarBonus(List<ContributedRepoDocument> repos) {
        boolean hasHighStarRepo = repos.stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsOwner()))
            .anyMatch(r -> r.getStargazersCount() != null && r.getStargazersCount() >= STAR_THRESHOLD_FOR_OWNED_REPO);

        return hasHighStarRepo ? BigDecimal.valueOf(2) : BigDecimal.ZERO;
    }

    private BigDecimal calculateHighStarPrBonus(List<PullRequestDocument> prs) {
        boolean hasMergedPrToHighStarRepo = prs.stream()
            .filter(pr -> Boolean.TRUE.equals(pr.getMerged()))
            .anyMatch(pr -> pr.getRepoStarCount() != null && pr.getRepoStarCount() >= STAR_THRESHOLD_FOR_IMPACT);

        return hasMergedPrToHighStarRepo ? BigDecimal.valueOf(1.5) : BigDecimal.ZERO;
    }

    private BigDecimal calculateClosedIssuesBonus(List<PullRequestDocument> prs) {
        int totalClosedIssues = prs.stream()
            .filter(pr -> Boolean.TRUE.equals(pr.getMerged()))
            .mapToInt(pr -> pr.getClosedIssuesCount() != null ? pr.getClosedIssuesCount() : 0)
            .sum();

        return totalClosedIssues >= CLOSED_ISSUES_THRESHOLD ? BigDecimal.valueOf(1) : BigDecimal.ZERO;
    }

    private BigDecimal calculateCrossRepoPrBonus(List<PullRequestDocument> prs) {
        boolean hasCrossRepoPrMerged = prs.stream()
            .filter(pr -> Boolean.TRUE.equals(pr.getMerged()))
            .anyMatch(pr -> Boolean.TRUE.equals(pr.getIsCrossRepository()));

        return hasCrossRepoPrMerged ? BigDecimal.valueOf(0.5) : BigDecimal.ZERO;
    }

    private void saveScores(String githubId, BigDecimal activityScore, BigDecimal diversityScore, BigDecimal impactScore) {
        GithubUserStatistics statistics = statisticsRepository.getOrCreate(githubId);

        statistics.updateScores(activityScore, diversityScore, impactScore);

        statisticsRepository.save(statistics);
    }

    private static class RepoStats {
        int commitCount = 0;
        int prCount = 0;
    }
}
