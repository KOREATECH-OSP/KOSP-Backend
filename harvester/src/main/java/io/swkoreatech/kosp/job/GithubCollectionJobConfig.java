package io.swkoreatech.kosp.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swkoreatech.kosp.collection.step.impl.ChallengeEvaluationStep;
import io.swkoreatech.kosp.collection.step.impl.CleanupStep;
import io.swkoreatech.kosp.collection.step.impl.CommitMiningStep;
import io.swkoreatech.kosp.collection.step.impl.IssueMiningStep;
import io.swkoreatech.kosp.collection.step.impl.PlatformAverageStep;
import io.swkoreatech.kosp.collection.step.impl.PullRequestMiningStep;
import io.swkoreatech.kosp.collection.step.impl.RepositoryDiscoveryStep;
import io.swkoreatech.kosp.collection.step.impl.ScoreCalculationStep;
import io.swkoreatech.kosp.collection.step.impl.StatisticsAggregationStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GithubCollectionJobConfig {

    private final JobRepository jobRepository;
    private final JobSchedulingListener jobSchedulingListener;

    private final RepositoryDiscoveryStep repositoryDiscoveryStep;
    private final CommitMiningStep commitMiningStep;
    private final PullRequestMiningStep pullRequestMiningStep;
    private final IssueMiningStep issueMiningStep;
    private final StatisticsAggregationStep statisticsAggregationStep;
    private final ScoreCalculationStep scoreCalculationStep;
    private final ChallengeEvaluationStep challengeEvaluationStep;
    private final PlatformAverageStep platformAverageStep;
    private final CleanupStep cleanupStep;

    @Bean
    public Job githubCollectionJob() {
        return new JobBuilder("githubCollectionJob", jobRepository)
            .listener(jobSchedulingListener)
            .start(repositoryDiscoveryStep.getStep())
            .next(pullRequestMiningStep.getStep())
            .next(issueMiningStep.getStep())
            .next(commitMiningStep.getStep())
            .next(statisticsAggregationStep.getStep())
            .next(scoreCalculationStep.getStep())
            .next(challengeEvaluationStep.getStep())
            .next(platformAverageStep.getStep())
            .next(cleanupStep.getStep())
            .build();
    }
}
