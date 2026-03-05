package io.swkoreatech.kosp.job;

import io.swkoreatech.kosp.collection.step.impl.ChallengeEvaluationStep;
import io.swkoreatech.kosp.collection.step.impl.CleanupStep;
import io.swkoreatech.kosp.collection.step.impl.CommitMiningStep;
import io.swkoreatech.kosp.collection.step.impl.IssueMiningStep;
import io.swkoreatech.kosp.collection.step.impl.PlatformAverageStep;
import io.swkoreatech.kosp.collection.step.impl.PullRequestMiningStep;
import io.swkoreatech.kosp.collection.step.impl.RepositoryDiscoveryStep;
import io.swkoreatech.kosp.collection.step.impl.ScoreCalculationStep;
import io.swkoreatech.kosp.collection.step.impl.StatisticsAggregationStep;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * GitHub 수집 배치 잡 설정 클래스.
 *
 * <p>저장소 발견, 커밋/PR/이슈 마이닝, 통계 집계, 점수 계산,
 * 챌린지 평가, 플랫폼 평균 계산, 정리 단계로 구성된 배치 잡을 정의한다.
 */
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

    /**
     * GitHub 수집 배치 잡 빈을 생성한다.
     *
     * @return 전체 수집 파이프라인이 구성된 Job 인스턴스
     */
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
