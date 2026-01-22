package io.swkoreatech.kosp.harvester.collection.step.impl;

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

import io.swkoreatech.kosp.harvester.client.GithubGraphQLClient;
import io.swkoreatech.kosp.harvester.client.dto.GraphQLResponse;
import io.swkoreatech.kosp.harvester.client.dto.UserPullRequestsResponse;
import io.swkoreatech.kosp.harvester.client.dto.UserPullRequestsResponse.PageInfo;
import io.swkoreatech.kosp.harvester.client.dto.UserPullRequestsResponse.PullRequestNode;
import io.swkoreatech.kosp.harvester.collection.document.PullRequestDocument;
import io.swkoreatech.kosp.harvester.collection.repository.PullRequestDocumentRepository;
import io.swkoreatech.kosp.harvester.collection.step.StepProvider;
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

    @Override
    public Step getStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet((contribution, chunkContext) -> {
                execute(chunkContext);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Override
    public String getStepName() {
        return STEP_NAME;
    }

    private void execute(ChunkContext chunkContext) {
        ExecutionContext context = getExecutionContext(chunkContext);
        Long userId = extractUserId(chunkContext);
        String login = context.getString("githubLogin");
        String token = context.getString("githubToken");

        if (login == null || token == null) {
            log.warn("GitHub credentials not found in context for user {}", userId);
            return;
        }

        int totalPrs = fetchAllPullRequests(userId, login, token);
        log.info("Mined {} PRs for user {}", totalPrs, userId);
    }

    private ExecutionContext getExecutionContext(ChunkContext chunkContext) {
        return chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getExecutionContext();
    }

    private Long extractUserId(ChunkContext chunkContext) {
        return chunkContext.getStepContext()
            .getStepExecution()
            .getJobParameters()
            .getLong("userId");
    }

    private int fetchAllPullRequests(Long userId, String login, String token) {
        int saved = 0;
        String cursor = null;
        Instant now = Instant.now();

        do {
            GraphQLResponse<UserPullRequestsResponse> response = fetchPullRequestsPage(login, cursor, token);
            if (response == null || response.hasErrors()) {
                logErrors(response, login);
                break;
            }

            UserPullRequestsResponse data = response.getDataAs(UserPullRequestsResponse.class);
            List<PullRequestNode> prs = data.getPullRequests();
            saved += savePullRequests(userId, prs, now);

            PageInfo pageInfo = data.getPageInfo();
            if (pageInfo == null || !pageInfo.isHasNextPage()) {
                break;
            }
            cursor = pageInfo.getEndCursor();
        } while (cursor != null);

        return saved;
    }

    private GraphQLResponse<UserPullRequestsResponse> fetchPullRequestsPage(String login, String cursor, String token) {
        return graphQLClient.getUserPullRequests(login, cursor, token, createResponseType()).block();
    }

    @SuppressWarnings("unchecked")
    private Class<GraphQLResponse<UserPullRequestsResponse>> createResponseType() {
        return (Class<GraphQLResponse<UserPullRequestsResponse>>) (Class<?>) GraphQLResponse.class;
    }

    private void logErrors(GraphQLResponse<UserPullRequestsResponse> response, String login) {
        if (response == null) {
            log.warn("No response from GraphQL for user {}", login);
            return;
        }
        log.error("GraphQL errors for user {}: {}", login, response.getErrors());
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
