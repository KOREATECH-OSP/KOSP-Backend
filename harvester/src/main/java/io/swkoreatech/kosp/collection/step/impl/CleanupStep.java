package io.swkoreatech.kosp.collection.step.impl;

import java.time.Instant;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.collection.document.CollectionMetadataDocument;
import io.swkoreatech.kosp.collection.repository.CollectionMetadataRepository;
import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.job.StepCompletionListener;
import io.swkoreatech.kosp.user.GithubUserRepository;
import io.swkoreatech.kosp.user.User;
import io.swkoreatech.kosp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupStep implements StepProvider {

    private static final String STEP_NAME = "cleanupStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final GithubUserRepository githubUserRepository;
    private final CollectionMetadataRepository metadataRepository;
    private final StepCompletionListener stepCompletionListener;

    @Override
    public Step getStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet((contribution, chunkContext) -> {
                Long userId = extractUserId(chunkContext);
                execute(userId, chunkContext);
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

    private void execute(Long userId, ChunkContext chunkContext) {
        updateLastCrawling(userId);
        updateCollectionMetadata(userId);
        clearExecutionContext(chunkContext);

        log.info("Cleanup completed for user {}", userId);
    }

    private void updateLastCrawling(Long userId) {
        User user = userRepository.getById(userId);
        if (!user.hasGithubUser()) {
            return;
        }

        GithubUser githubUser = user.getGithubUser();
        githubUser.updateLastCrawling();
        githubUserRepository.save(githubUser);
    }

    private void updateCollectionMetadata(Long userId) {
        CollectionMetadataDocument metadata = metadataRepository.getByUserId(userId);

        CollectionMetadataDocument updated = CollectionMetadataDocument.builder()
            .id(metadata.getId())
            .userId(userId)
            .lastFullCollection(Instant.now())
            .lastIncrementalCollection(metadata.getLastIncrementalCollection())
            .lastCommitCursor(metadata.getLastCommitCursor())
            .lastPrCursor(metadata.getLastPrCursor())
            .lastIssueCursor(metadata.getLastIssueCursor())
            .createdAt(metadata.getCreatedAt())
            .updatedAt(Instant.now())
            .build();

        metadataRepository.save(updated);
    }

    private void clearExecutionContext(ChunkContext chunkContext) {
        chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getExecutionContext()
            .remove("discoveredRepos");
    }
}
