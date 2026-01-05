package kr.ac.koreatech.sw.kosp.domain.github.batch;


import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GithubSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;

    private final UserSyncWriter userSyncWriter;
    private final GithubActivityProcessor githubActivityProcessor;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job githubSyncJob() {
        return new JobBuilder("githubSyncJob", jobRepository)
            .start(githubSyncStep())
            .build();
    }

    @Bean
    public Step githubSyncStep() {
        return new StepBuilder("githubSyncStep", jobRepository)
            .<User, kr.ac.koreatech.sw.kosp.domain.github.dto.UserSyncResult>chunk(CHUNK_SIZE, transactionManager)
            .reader(userReader())
            .processor(githubActivityProcessor)
            .writer(userSyncWriter)
            .taskExecutor(taskExecutor()) // Enable Parallel Processing
            .build();
    }

    @Bean
    public RepositoryItemReader<User> userReader() {
        return new RepositoryItemReaderBuilder<User>()
            .name("userReader")
            .repository(userRepository)
            .methodName("findAll")
            .pageSize(CHUNK_SIZE)
            .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
            .build();
    }

    @Bean
    public org.springframework.core.task.TaskExecutor taskExecutor() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor = new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // 4 concurrent threads
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("github-sync-");
        executor.initialize();
        return executor;
    }
    
    // Removed MongoItemWriter bean as we use custom UserSyncWriter

}
