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
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubDataCollectionRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataCollectionJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GithubDataCollectionRetryService dataCollectionRetryService;
    private final GithubUserRepository githubUserRepository;
    
    @Bean
    public Job dataCollectionJob(Step collectDataStep) {
        return new JobBuilder("dataCollectionJob", jobRepository)
            .start(collectDataStep)
            .build();
    }
    
    @Bean
    public Step collectDataStep() {
        return new StepBuilder("collectDataStep", jobRepository)
            .<GithubUser, GithubUser>chunk(10, transactionManager)
            .reader(githubUserReader())
            .processor(dataCollectionProcessor())
            .writer(dataCollectionWriter())
            .build();
    }
    
    @Bean
    public RepositoryItemReader<GithubUser> githubUserReader() {
        return new RepositoryItemReaderBuilder<GithubUser>()
            .name("githubUserReader")
            .repository(githubUserRepository)
            .methodName("findAll")
            .pageSize(10)
            .sorts(Map.of("githubId", Sort.Direction.ASC))
            .build();
    }
    
    @Bean
    public ItemProcessor<GithubUser, GithubUser> dataCollectionProcessor() {
        return githubUser -> {
            try {
                log.info("Processing data collection for user: {}", githubUser.getGithubLogin());
                
                dataCollectionRetryService.collectWithRetry(
                    githubUser.getGithubLogin(),
                    githubUser.getGithubToken()
                );
                
                return githubUser;
            } catch (Exception exception) {
                log.error("Failed to collect data for user: {}",
                    githubUser.getGithubLogin(), exception);
                return null;
            }
        };
    }
    
    @Bean
    public ItemWriter<GithubUser> dataCollectionWriter() {
        return items -> {
            log.info("Completed data collection for {} users", items.size());
        };
    }
}
