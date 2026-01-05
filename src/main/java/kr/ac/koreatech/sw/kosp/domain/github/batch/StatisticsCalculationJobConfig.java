package kr.ac.koreatech.sw.kosp.domain.github.batch;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StatisticsCalculationJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GithubStatisticsService statisticsService;
    private final GithubUserRepository githubUserRepository;
    
    @Bean
    public Job statisticsCalculationJob(Step calculateStatisticsStep) {
        return new JobBuilder("statisticsCalculationJob", jobRepository)
            .start(calculateStatisticsStep)
            .build();
    }
    
    @Bean
    public Step calculateStatisticsStep() {
        return new StepBuilder("calculateStatisticsStep", jobRepository)
            .<GithubUser, GithubUser>chunk(10, transactionManager)
            .reader(githubUserReaderForStats())
            .processor(statisticsProcessor())
            .writer(statisticsWriter())
            .build();
    }
    
    @Bean
    public RepositoryItemReader<GithubUser> githubUserReaderForStats() {
        return new RepositoryItemReaderBuilder<GithubUser>()
            .name("githubUserReaderForStats")
            .repository(githubUserRepository)
            .methodName("findAll")
            .pageSize(10)
            .sorts(Map.of("githubId", Sort.Direction.ASC))
            .build();
    }
    
    @Bean
    public ItemProcessor<GithubUser, GithubUser> statisticsProcessor() {
        return githubUser -> {
            try {
                statisticsService.calculateAndSaveAllStatistics(
                    githubUser.getGithubLogin()
                );
                return githubUser;
            } catch (Exception e) {
                log.error("Failed to calculate statistics for user: {}", 
                    githubUser.getGithubLogin(), e);
                return null;
            }
        };
    }
    
    @Bean
    public ItemWriter<GithubUser> statisticsWriter() {
        return items -> {
            log.info("Completed statistics calculation for {} users", items.size());
        };
    }
}
