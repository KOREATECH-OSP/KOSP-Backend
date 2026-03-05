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
import io.swkoreatech.kosp.collection.step.StepContextKeys;
import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.collection.util.StepContextHelper;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.job.StepCompletionListener;
import io.swkoreatech.kosp.user.GithubUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 잡 완료 후 임시 실행 컨텍스트 데이터를 정리하는 스텝.
 *
 * <p>ExecutionContext에서 민감한 인증 정보와 임시 데이터를 제거하고,
 * 수집 메타데이터를 갱신하며, 다음 실행을 위한 깨끗한 상태를 보장한다.
 *
 * @StepContract
 * REQUIRES: discoveredRepos (정리 추적용)
 * PROVIDES: (없음 - 최종 정리 스텝)
 */

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

    /** {@inheritDoc} */
    @Override
    public Step getStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet((contribution, chunkContext) -> {
                Long userId = StepContextHelper.extractUserId(chunkContext);
                execute(userId, chunkContext);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .listener(stepCompletionListener)
            .build();
    }

    /** {@inheritDoc} */
    @Override
    public String getStepName() {
        return STEP_NAME;
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
        CollectionMetadataDocument updated = buildUpdatedMetadata(metadata, userId);
        metadataRepository.save(updated);
    }

    private CollectionMetadataDocument buildUpdatedMetadata(CollectionMetadataDocument metadata, Long userId) {
        Instant now = Instant.now();
        return CollectionMetadataDocument.builder()
            .id(metadata.getId()).userId(userId).lastFullCollection(now)
            .lastIncrementalCollection(metadata.getLastIncrementalCollection())
            .lastCommitCursor(metadata.getLastCommitCursor())
            .lastPrCursor(metadata.getLastPrCursor())
            .lastIssueCursor(metadata.getLastIssueCursor())
            .createdAt(metadata.getCreatedAt()).updatedAt(now).build();
    }

    private void clearExecutionContext(ChunkContext chunkContext) {
        chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getExecutionContext()
            .remove(StepContextKeys.DISCOVERED_REPOS);
    }
}
