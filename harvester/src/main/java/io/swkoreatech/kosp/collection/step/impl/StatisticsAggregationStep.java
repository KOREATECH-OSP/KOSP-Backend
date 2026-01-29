package io.swkoreatech.kosp.collection.step.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.collection.document.CommitDocument;
import io.swkoreatech.kosp.collection.document.ContributedRepoDocument;
import io.swkoreatech.kosp.collection.document.IssueDocument;
import io.swkoreatech.kosp.collection.document.PullRequestDocument;
import io.swkoreatech.kosp.collection.entity.GithubRepositoryStatistics;
import io.swkoreatech.kosp.collection.repository.CommitDocumentRepository;
import io.swkoreatech.kosp.collection.repository.ContributedRepoDocumentRepository;
import io.swkoreatech.kosp.collection.repository.GithubRepositoryStatisticsRepository;
import io.swkoreatech.kosp.collection.repository.IssueDocumentRepository;
import io.swkoreatech.kosp.collection.repository.PullRequestDocumentRepository;
import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.collection.util.NullSafeGetters;
import io.swkoreatech.kosp.collection.util.StepContextHelper;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.github.model.GithubUserStatistics;
import io.swkoreatech.kosp.job.StepCompletionListener;
import io.swkoreatech.kosp.statistics.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.user.User;
import io.swkoreatech.kosp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * Aggregates platform-wide statistics from collected GitHub data.
 *
 * @StepContract
 * REQUIRES: (none - reads from MongoDB collections)
 * PROVIDES: (none - writes to MongoDB statistics collection)
 * PURPOSE: Calculates aggregate metrics across all users including total commits,
 *          PRs, issues, and repository counts for platform analytics dashboard.
 */

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
    private final GithubRepositoryStatisticsRepository repoStatsRepository;
    private final StepCompletionListener stepCompletionListener;

    @Override
    public Step getStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet((contribution, chunkContext) -> {
                Long userId = StepContextHelper.extractUserId(chunkContext);
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

    private void execute(Long userId) {
        User user = userRepository.getById(userId);
        if (!user.hasGithubUser()) {
            log.warn("User {} does not have GitHub account linked", userId);
            return;
        }

        GithubUser githubUser = user.getGithubUser();
        String githubId = String.valueOf(githubUser.getGithubId());

        AggregatedStats stats = aggregateFromMongoDB(userId);
        saveToDB(githubId, stats);
        updateContributedRepoStats(userId);
        saveRepositoriesToPostgreSQL(userId, githubId);

        log.info("Aggregated statistics for user {}: {} commits, {} PRs, {} issues",
            userId, stats.totalCommits, stats.totalPrs, stats.totalIssues);
    }

     private AggregatedStats aggregateFromMongoDB(Long userId) {
         List<CommitDocument> commits = commitDocumentRepository.findByUserId(userId);
         List<PullRequestDocument> prs = prDocumentRepository.findByUserId(userId);
         List<IssueDocument> issues = issueDocumentRepository.findByUserId(userId);
         List<ContributedRepoDocument> repos = repoDocumentRepository.findByUserId(userId);

         CalculationResults results = calculateAllMetrics(commits, repos);
         return buildAggregatedStats(commits, prs, issues, repos, results);
     }

    private boolean isNightCommit(CommitDocument commit) {
        if (commit.getAuthoredAt() == null) {
            return false;
        }
        int hour = commit.getAuthoredAt().atZone(java.time.ZoneId.systemDefault()).getHour();
        return hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR;
    }

    private void saveToDB(String githubId, AggregatedStats stats) {
        GithubUserStatistics statistics = statisticsRepository.getOrCreate(githubId);
        updateStatisticsFields(statistics, stats);
        updateDataPeriod(statistics);
        statisticsRepository.save(statistics);
    }

    private void updateStatisticsFields(GithubUserStatistics statistics, AggregatedStats stats) {
        statistics.updateStatistics(stats.totalCommits, stats.totalLines, stats.totalAdditions,
            stats.totalDeletions, stats.totalPrs, stats.totalIssues, stats.ownedReposCount,
            stats.contributedReposCount, stats.totalStarsReceived, stats.totalForksReceived,
            stats.nightCommits, stats.dayCommits);
    }

    private void updateDataPeriod(GithubUserStatistics statistics) {
        statistics.updateDataPeriod(
            LocalDate.now().minusYears(1),
            LocalDate.now()
        );
    }

     private void updateContributedRepoStats(Long userId) {
         UserActivityData data = fetchAllUserData(userId);
         updateAllRepos(data.repos, data.commits, data.prs, data.issues);
         repoDocumentRepository.saveAll(data.repos);
     }

     private UserActivityData fetchAllUserData(Long userId) {
         List<ContributedRepoDocument> repos = repoDocumentRepository.findByUserId(userId);
         List<CommitDocument> commits = commitDocumentRepository.findByUserId(userId);
         List<PullRequestDocument> prs = prDocumentRepository.findByUserId(userId);
         List<IssueDocument> issues = issueDocumentRepository.findByUserId(userId);
         return new UserActivityData(repos, commits, prs, issues);
     }

     private void updateAllRepos(
         List<ContributedRepoDocument> repos,
         List<CommitDocument> commits,
         List<PullRequestDocument> prs,
         List<IssueDocument> issues
     ) {
         for (ContributedRepoDocument repo : repos) {
             updateSingleRepoStats(repo, commits, prs, issues);
         }
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

      private CalculationResults calculateAllMetrics(List<CommitDocument> commits, List<ContributedRepoDocument> repos) {
         int totalAdditions = calculateTotalAdditions(commits);
         int totalDeletions = calculateTotalDeletions(commits);
         int nightCommits = calculateNightCommits(commits);
         int ownedRepos = calculateOwnedReposCount(repos);
         int totalStars = calculateTotalStars(repos);
         int totalForks = calculateTotalForks(repos);
         return new CalculationResults(totalAdditions, totalDeletions, nightCommits, ownedRepos, totalStars, totalForks);
     }

      private int calculateTotalAdditions(List<CommitDocument> commits) {
          return commits.stream()
              .mapToInt(c -> NullSafeGetters.intOrZero(c.getAdditions()))
              .sum();
      }

      private int calculateTotalDeletions(List<CommitDocument> commits) {
          return commits.stream()
              .mapToInt(c -> NullSafeGetters.intOrZero(c.getDeletions()))
              .sum();
      }

     private int calculateNightCommits(List<CommitDocument> commits) {
         return (int) commits.stream()
             .filter(this::isNightCommit)
             .count();
     }

     private int calculateOwnedReposCount(List<ContributedRepoDocument> repos) {
         return (int) repos.stream()
             .filter(r -> Boolean.TRUE.equals(r.getIsOwner()))
             .count();
     }

      private int calculateTotalStars(List<ContributedRepoDocument> repos) {
          return repos.stream()
              .filter(r -> Boolean.TRUE.equals(r.getIsOwner()))
              .mapToInt(r -> NullSafeGetters.intOrZero(r.getStargazersCount()))
              .sum();
      }

      private int calculateTotalForks(List<ContributedRepoDocument> repos) {
          return repos.stream()
              .filter(r -> Boolean.TRUE.equals(r.getIsOwner()))
              .mapToInt(r -> NullSafeGetters.intOrZero(r.getForksCount()))
              .sum();
      }

     private AggregatedStats buildAggregatedStats(
         List<CommitDocument> commits,
         List<PullRequestDocument> prs,
         List<IssueDocument> issues,
         List<ContributedRepoDocument> repos,
         CalculationResults results
     ) {
         int dayCommits = commits.size() - results.nightCommits;
         return new AggregatedStats(commits.size(), results.totalAdditions + results.totalDeletions, results.totalAdditions, results.totalDeletions, prs.size(), issues.size(), results.ownedRepos, repos.size(), results.totalStars, results.totalForks, results.nightCommits, dayCommits);
     }

     private void saveRepositoriesToPostgreSQL(Long userId, String githubId) {
         List<ContributedRepoDocument> repos = repoDocumentRepository.findByUserId(userId);
         
         for (ContributedRepoDocument repo : repos) {
             saveOrUpdateRepoStats(repo, githubId);
         }
         
         log.info("Saved {} repositories to PostgreSQL for user {}", repos.size(), userId);
     }

     private void saveOrUpdateRepoStats(ContributedRepoDocument repo, String githubId) {
         GithubRepositoryStatistics stats = repoStatsRepository
             .findByRepoOwnerAndRepoNameAndContributorGithubId(
                 repo.getRepositoryOwner(),
                 repo.getRepositoryName(),
                 githubId
             )
             .orElse(GithubRepositoryStatistics.create(
                 repo.getRepositoryOwner(),
                 repo.getRepositoryName(),
                 githubId
             ));
         
         stats.updateRepositoryInfo(
             defaultToZero(repo.getStargazersCount()),
             defaultToZero(repo.getForksCount()),
             defaultToZero(repo.getWatchersCount()),
             repo.getDescription(),
             repo.getPrimaryLanguage(),
             convertToLocalDateTime(repo.getRepoCreatedAt())
         );
         
         stats.updateOwnership(repo.getIsOwner());
         
         stats.updateUserContributions(
             defaultToZero(repo.getUserCommitCount()),
             defaultToZero(repo.getUserPrCount()),
             defaultToZero(repo.getUserIssueCount()),
             convertToLocalDateTime(repo.getLastContributedAt())
         );
         
         stats.updateTotalCounts(0, 0, 0);
         
         repoStatsRepository.save(stats);
     }

     private LocalDateTime convertToLocalDateTime(Instant instant) {
         if (instant == null) {
             return null;
         }
         return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
     }

     private Integer defaultToZero(Integer value) {
         return value != null ? value : 0;
     }

      private record CalculationResults(
          int totalAdditions,
          int totalDeletions,
          int nightCommits,
          int ownedRepos,
          int totalStars,
          int totalForks
      ) {}

      private record UserActivityData(
          List<ContributedRepoDocument> repos,
          List<CommitDocument> commits,
          List<PullRequestDocument> prs,
          List<IssueDocument> issues
      ) {}

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
