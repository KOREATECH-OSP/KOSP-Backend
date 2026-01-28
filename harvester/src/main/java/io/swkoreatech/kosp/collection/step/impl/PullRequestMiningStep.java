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
import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.collection.util.GraphQLTypeFactory;
import io.swkoreatech.kosp.collection.util.PaginationHelper;
import io.swkoreatech.kosp.collection.util.StepContextHelper;
import io.swkoreatech.kosp.job.StepCompletionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @Override
    public String getStepName() {
        return STEP_NAME;
    }

    private void execute(ChunkContext chunkContext) {
        ExecutionContext context = StepContextHelper.getExecutionContext(chunkContext);
        Long userId = StepContextHelper.extractUserId(chunkContext);
        String login = context.getString("githubLogin");
        String token = context.getString("githubToken");

        if (login == null || token == null) {
            log.warn("GitHub credentials not found in context for user {}", userId);
            return;
        }

        int totalPrs = fetchAllPullRequests(userId, login, token);
        log.info("Mined {} PRs for user {}", totalPrs, userId);
    }

    private int fetchAllPullRequests(Long userId, String login, String token) {
        Instant now = Instant.now();
        return PaginationHelper.paginate(
            cursor -> fetchPullRequestsPage(login, cursor, token),
            UserPullRequestsResponse::getPageInfo,
            (data, cursor) -> savePullRequests(userId, data.getPullRequests(), now),
            "pullRequest",
            login,
            UserPullRequestsResponse.class
        );
    }

    private GraphQLResponse<UserPullRequestsResponse> fetchPullRequestsPage(String login, String cursor, String token) {
        return graphQLClient.getUserPullRequests(login, cursor, token, GraphQLTypeFactory.<UserPullRequestsResponse>responseType()).block();
    }

    private int savePullRequests(Long userId, List<PullRequestNode> prs, Instant now) {
        int saved = 0;
        for (PullRequestNode pr : prs) {
            if (prDocumentRepository.existsByUserIdAndPrNumber(userId, pr.getNumber())) {
                continue;
            }

            PullRequestDocument document = buildDocument(userId, pr, now);
            prDocumentRepository.save(document);
            saved++;
        }
        return saved;
    }

    private PullRequestDocument buildDocument(Long userId, PullRequestNode pr, Instant now) {
        return PullRequestDocument.builder()
            .userId(userId)
            .prNumber(pr.getNumber())
            .title(pr.getTitle())
            .state(pr.getState())
            .repositoryName(pr.getRepoName())
            .repositoryOwner(pr.getRepoOwner())
            .additions(pr.getAdditions())
            .deletions(pr.getDeletions())
            .changedFiles(pr.getChangedFiles())
            .commitsCount(pr.getCommitsCount())
            .repoStarCount(pr.getRepoStarCount())
            .closedIssuesCount(pr.getClosedIssuesCount())
            .merged(pr.isMerged())
            .isCrossRepository(pr.isCrossRepository())
            .mergedAt(pr.getMergedAt())
            .createdAt(pr.getCreatedAt())
            .closedAt(pr.getClosedAt())
            .collectedAt(now)
            .build();
    }
}
