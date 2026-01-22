package io.swkoreatech.kosp.harvester.job;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.harvester.collection.step.StepRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GithubCollectionJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobSchedulingListener jobSchedulingListener;
    private final StepRegistry stepRegistry;

    @Bean
    public Job githubCollectionJob() {
        List<Step> steps = stepRegistry.getOrderedSteps();

        if (steps.isEmpty()) {
            return buildJobWithPlaceholder();
        }

        return buildJobWithSteps(steps);
    }

    private Job buildJobWithPlaceholder() {
        return new JobBuilder("githubCollectionJob", jobRepository)
            .listener(jobSchedulingListener)
            .start(placeholderStep())
            .build();
    }

    private Job buildJobWithSteps(List<Step> steps) {
        SimpleJobBuilder jobBuilder = new JobBuilder("githubCollectionJob", jobRepository)
            .listener(jobSchedulingListener)
            .start(steps.get(0));

        for (int i = 1; i < steps.size(); i++) {
            jobBuilder.next(steps.get(i));
        }

        return jobBuilder.build();
    }

    private Step placeholderStep() {
        return new StepBuilder("placeholderStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                Long userId = chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobParameters()
                    .getLong("userId");
                log.info("Placeholder step executed for user {}. Register StepProvider beans to add actual steps.", userId);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
