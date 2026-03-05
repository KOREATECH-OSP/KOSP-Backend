package io.swkoreatech.kosp.collection.step.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.client.GithubGraphQLClient;
import io.swkoreatech.kosp.client.dto.GraphQLResponse;
import io.swkoreatech.kosp.client.dto.UserPullRequestsResponse;
import io.swkoreatech.kosp.client.dto.UserPullRequestsResponse.PullRequestNode;
import io.swkoreatech.kosp.collection.document.PullRequestDocument;
import io.swkoreatech.kosp.collection.repository.PullRequestDocumentRepository;
import io.swkoreatech.kosp.collection.step.StepContextKeys;
import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.collection.util.GraphQLTypeFactory;
import io.swkoreatech.kosp.collection.util.PaginationHelper;
import io.swkoreatech.kosp.collection.util.StepContextHelper;
import io.swkoreatech.kosp.job.LoggingConstants;
import io.swkoreatech.kosp.job.StepCompletionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자가 생성한 풀 리퀘스트 데이터를 마이닝하는 스텝.
 *
 * <p>GraphQL 페이지네이션을 사용하여 사용자의 모든 PR을 조회하고,
 * PullRequestDocument 컬렉션에 저장한다. 기여 분석에 사용된다.
 *
 * @StepContract
 * REQUIRES: githubLogin, githubToken (RepositoryDiscoveryStep에서)
 * PROVIDES: (없음 - MongoDB에만 기록)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PullRequestMiningStep implements StepProvider {

    private static final String STEP_NAME = "pullRequestMiningStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GithubGraphQLClient graphQLClient;
    private final PullRequestDocumentRepository prDocumentRepository;
    private final StepCompletionListener stepCompletionListener;

    /** {@inheritDoc} */
    @Override
    public Step getStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet((contribution, chunkContext) -> {
                execute(chunkContext);
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

    private int totalSavedCount;
    private int totalSkippedCount;

    private void execute(ChunkContext chunkContext) {
        ExecutionContext context = StepContextHelper.getExecutionContext(chunkContext);
        Long userId = StepContextHelper.extractUserId(chunkContext);
        String login = context.getString(StepContextKeys.GITHUB_LOGIN);
        String token = context.getString(StepContextKeys.GITHUB_TOKEN);

        if (login == null || token == null) {
            log.warn("GitHub credentials not found in context for user {}", userId);
            return;
        }

        totalSavedCount = 0;
        totalSkippedCount = 0;
        int totalMined = fetchAllPullRequests(userId, login, token);
        log.info(LoggingConstants.MINING_SUMMARY, totalMined, totalSavedCount, totalSkippedCount);
    }

    private int fetchAllPullRequests(Long userId, String login, String token) {
        Instant now = Instant.now();
        return PaginationHelper.paginate(
            cursor -> fetchPullRequestsPage(login, cursor, token),
            UserPullRequestsResponse::getPageInfo,
            (data, c) -> savePullRequests(userId, data.getPullRequests(), now),
            "user",
            login,
            UserPullRequestsResponse.class
        );
    }

    private GraphQLResponse<UserPullRequestsResponse> fetchPullRequestsPage(String login, String cursor, String token) {
        return graphQLClient.getUserPullRequests(login, cursor, token,
            GraphQLTypeFactory.<UserPullRequestsResponse>responseType()).block();
    }

    private int savePullRequests(Long userId, List<PullRequestNode> prs, Instant now) {
        int saved = 0;
        int skipped = 0;
        for (PullRequestNode pr : prs) {
            if (pr == null) {
                skipped++;
                continue;
            }
            if (prDocumentRepository.existsByUserIdAndRepositoryNameAndPrNumber(userId, pr.getRepoName(),
                pr.getNumber())) {
                skipped++;
                continue;
            }

            PullRequestDocument document = buildDocument(userId, pr, now);
            prDocumentRepository.save(document);
            saved++;
        }
        totalSavedCount += saved;
        totalSkippedCount += skipped;
        return saved + skipped;
    }

    private PullRequestDocument buildDocument(Long userId, PullRequestNode pr, Instant now) {
        PullRequestDocument.PullRequestDocumentBuilder builder = PullRequestDocument.builder();
        builder = buildBasicFields(builder, userId, pr);
        builder = buildStatisticsFields(builder, pr);
        builder = buildMetadataFields(builder, pr, now);
        return builder.build();
    }

    private PullRequestDocument.PullRequestDocumentBuilder buildBasicFields(
        PullRequestDocument.PullRequestDocumentBuilder builder,
        Long userId,
        PullRequestNode pr) {
        return builder
            .userId(userId)
            .prNumber(pr.getNumber())
            .title(pr.getTitle())
            .state(pr.getState())
            .repositoryName(pr.getRepoName())
            .repositoryOwner(pr.getRepoOwner());
    }

    private PullRequestDocument.PullRequestDocumentBuilder buildStatisticsFields(
        PullRequestDocument.PullRequestDocumentBuilder builder,
        PullRequestNode pr) {
        return builder
            .additions(pr.getAdditions())
            .deletions(pr.getDeletions())
            .changedFiles(pr.getChangedFiles())
            .commitsCount(pr.getCommitsCount())
            .repoStarCount(pr.getRepoStarCount())
            .closedIssuesCount(pr.getClosedIssuesCount());
    }

    private PullRequestDocument.PullRequestDocumentBuilder buildMetadataFields(
        PullRequestDocument.PullRequestDocumentBuilder builder,
        PullRequestNode pr,
        Instant now) {
        return builder
            .merged(pr.isMerged())
            .isCrossRepository(pr.isCrossRepository())
            .mergedAt(pr.getMergedAt())
            .createdAt(pr.getCreatedAt())
            .closedAt(pr.getClosedAt())
            .collectedAt(now);
    }
}
