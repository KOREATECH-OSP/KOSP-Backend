package io.swkoreatech.kosp.collection.step.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.collection.document.CommitDocument;
import io.swkoreatech.kosp.collection.document.ContributedRepoDocument;
import io.swkoreatech.kosp.collection.document.IssueDocument;
import io.swkoreatech.kosp.collection.document.PullRequestDocument;
import io.swkoreatech.kosp.collection.repository.CommitDocumentRepository;
import io.swkoreatech.kosp.collection.repository.ContributedRepoDocumentRepository;
import io.swkoreatech.kosp.collection.repository.IssueDocumentRepository;
import io.swkoreatech.kosp.collection.repository.PullRequestDocumentRepository;
import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.job.StepCompletionListener;
import io.swkoreatech.kosp.statistics.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.user.User;
import io.swkoreatech.kosp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsAggregationStep implements StepProvider {

    private static final String STEP_NAME = "statisticsAggregationStep";
    private static final int NIGHT_START_HOUR = 22;
    private static final int NIGHT_END_HOUR = 6;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;

    private final CommitDocumentRepository commitDocumentRepository;
    private final PullRequestDocumentRepository prDocumentRepository;
    private final IssueDocumentRepository issueDocumentRepository;
    private final ContributedRepoDocumentRepository repoDocumentRepository;
    private final GithubUserStatisticsRepository statisticsRepository;
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

        AggregatedStats stats = aggregateFromMongoDB(userId);
        saveToMySQL(githubId, stats);
        updateContributedRepoStats(userId);

        log.info("Aggregated statistics for user {}: {} commits, {} PRs, {} issues",
            userId, stats.totalCommits, stats.totalPrs, stats.totalIssues);
    }

    private AggregatedStats aggregateFromMongoDB(Long userId) {
        List<CommitDocument> commits = commitDocumentRepository.findByUserId(userId);
        List<PullRequestDocument> prs = prDocumentRepository.findByUserId(userId);
        List<IssueDocument> issues = issueDocumentRepository.findByUserId(userId);
        List<ContributedRepoDocument> repos = repoDocumentRepository.findByUserId(userId);

        int totalAdditions = commits.stream()
            .mapToInt(c -> c.getAdditions() != null ? c.getAdditions() : 0)
            .sum();

        int totalDeletions = commits.stream()
            .mapToInt(c -> c.getDeletions() != null ? c.getDeletions() : 0)
            .sum();

        int nightCommits = (int) commits.stream()
            .filter(this::isNightCommit)
            .count();

        int ownedRepos = (int) repos.stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsOwner()))
            .count();

        int totalStars = repos.stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsOwner()))
            .mapToInt(r -> r.getStargazersCount() != null ? r.getStargazersCount() : 0)
            .sum();

        int totalForks = repos.stream()
            .filter(r -> Boolean.TRUE.equals(r.getIsOwner()))
            .mapToInt(r -> r.getForksCount() != null ? r.getForksCount() : 0)
            .sum();

        return new AggregatedStats(
            commits.size(),
            totalAdditions + totalDeletions,
            totalAdditions,
            totalDeletions,
            prs.size(),
            issues.size(),
            ownedRepos,
            repos.size(),
            totalStars,
            totalForks,
            nightCommits,
            commits.size() - nightCommits
        );
    }

    private boolean isNightCommit(CommitDocument commit) {
        if (commit.getAuthoredAt() == null) {
            return false;
        }
        int hour = commit.getAuthoredAt().atZone(java.time.ZoneId.systemDefault()).getHour();
        return hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR;
    }

    private void saveToMySQL(String githubId, AggregatedStats stats) {
        GithubUserStatistics statistics = statisticsRepository.getOrCreate(githubId);

        statistics.updateStatistics(
            stats.totalCommits,
            stats.totalLines,
            stats.totalAdditions,
            stats.totalDeletions,
            stats.totalPrs,
            stats.totalIssues,
            stats.ownedReposCount,
            stats.contributedReposCount,
            stats.totalStarsReceived,
            stats.totalForksReceived,
            stats.nightCommits,
            stats.dayCommits
        );

        statistics.updateDataPeriod(
            LocalDate.now().minusYears(1),
            LocalDate.now()
        );

        statisticsRepository.save(statistics);
    }

    private void updateContributedRepoStats(Long userId) {
        List<ContributedRepoDocument> repos = repoDocumentRepository.findByUserId(userId);
        List<CommitDocument> commits = commitDocumentRepository.findByUserId(userId);
        List<PullRequestDocument> prs = prDocumentRepository.findByUserId(userId);
        List<IssueDocument> issues = issueDocumentRepository.findByUserId(userId);

        for (ContributedRepoDocument repo : repos) {
            updateSingleRepoStats(repo, commits, prs, issues);
        }

        repoDocumentRepository.saveAll(repos);
    }

    private void updateSingleRepoStats(
        ContributedRepoDocument repo,
        List<CommitDocument> allCommits,
        List<PullRequestDocument> allPrs,
        List<IssueDocument> allIssues
    ) {
        String repoFullName = repo.getFullName();

        int commitCount = countCommitsForRepo(allCommits, repoFullName);
        int prCount = countPrsForRepo(allPrs, repoFullName);
        int issueCount = countIssuesForRepo(allIssues, repoFullName);
        Instant lastCommit = findLastCommitDate(allCommits, repoFullName);

        repo.updateUserStats(commitCount, prCount, issueCount, lastCommit);
    }

    private int countCommitsForRepo(List<CommitDocument> commits, String repoFullName) {
        return (int) commits.stream()
            .filter(c -> repoFullName.equals(c.getRepositoryOwner() + "/" + c.getRepositoryName()))
            .count();
    }

    private int countPrsForRepo(List<PullRequestDocument> prs, String repoFullName) {
        return (int) prs.stream()
            .filter(p -> repoFullName.equals(p.getRepositoryOwner() + "/" + p.getRepositoryName()))
            .count();
    }

    private int countIssuesForRepo(List<IssueDocument> issues, String repoFullName) {
        return (int) issues.stream()
            .filter(i -> repoFullName.equals(i.getRepositoryOwner() + "/" + i.getRepositoryName()))
            .count();
    }

    private Instant findLastCommitDate(List<CommitDocument> commits, String repoFullName) {
        return commits.stream()
            .filter(c -> repoFullName.equals(c.getRepositoryOwner() + "/" + c.getRepositoryName()))
            .map(CommitDocument::getAuthoredAt)
            .filter(Objects::nonNull)
            .max(Instant::compareTo)
            .orElse(null);
    }

    private record AggregatedStats(
        int totalCommits,
        int totalLines,
        int totalAdditions,
        int totalDeletions,
        int totalPrs,
        int totalIssues,
        int ownedReposCount,
        int contributedReposCount,
        int totalStarsReceived,
        int totalForksReceived,
        int nightCommits,
        int dayCommits
    ) {}
}
