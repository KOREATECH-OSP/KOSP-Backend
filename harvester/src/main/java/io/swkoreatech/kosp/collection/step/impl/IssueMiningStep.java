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
import io.swkoreatech.kosp.client.dto.UserIssuesResponse;
import io.swkoreatech.kosp.client.dto.UserIssuesResponse.IssueNode;
import io.swkoreatech.kosp.collection.document.IssueDocument;
import io.swkoreatech.kosp.collection.repository.IssueDocumentRepository;
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
 * 사용자가 생성한 이슈 데이터를 마이닝하는 스텝.
 *
 * <p>GraphQL 페이지네이션을 사용하여 사용자의 모든 이슈를 조회하고,
 * IssueDocument 컬렉션에 저장한다. 기여 추적에 사용된다.
 *
 * @StepContract
 * REQUIRES: githubLogin, githubToken (RepositoryDiscoveryStep에서)
 * PROVIDES: (없음 - MongoDB에만 기록)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IssueMiningStep implements StepProvider {

    private static final String STEP_NAME = "issueMiningStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GithubGraphQLClient graphQLClient;
    private final IssueDocumentRepository issueDocumentRepository;
    private final StepCompletionListener stepCompletionListener;

    private int totalSavedCount;
    private int totalSkippedCount;

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
        int totalMined = fetchAllIssues(userId, login, token);
        log.info(LoggingConstants.MINING_SUMMARY, totalMined, totalSavedCount, totalSkippedCount);
    }

    private int fetchAllIssues(Long userId, String login, String token) {
        Instant now = Instant.now();
        return PaginationHelper.paginate(
            cursor -> fetchIssuesPage(login, cursor, token),
            UserIssuesResponse::getPageInfo,
            (data, cursor) -> {
                int saved = saveIssues(userId, data.getIssues(), now);
                totalSavedCount += saved;
                totalSkippedCount += data.getIssues().size() - saved;
                return saved;
            },
            "user",
            login,
            UserIssuesResponse.class
        );
    }

    private GraphQLResponse<UserIssuesResponse> fetchIssuesPage(String login, String cursor, String token) {
        return graphQLClient.getUserIssues(login, cursor, token, GraphQLTypeFactory.<UserIssuesResponse>responseType())
            .block();
    }

    private int saveIssues(Long userId, List<IssueNode> issues, Instant now) {
        int saved = 0;
        for (IssueNode issue : issues) {
            if (issueDocumentRepository.existsByUserIdAndRepositoryNameAndIssueNumber(userId, issue.getRepoName(),
                issue.getNumber())) {
                continue;
            }

            IssueDocument document = buildDocument(userId, issue, now);
            issueDocumentRepository.save(document);
            saved++;
        }
        return saved;
    }

    private IssueDocument buildDocument(Long userId, IssueNode issue, Instant now) {
        IssueDocument.IssueDocumentBuilder builder = IssueDocument.builder();
        builder = buildBasicFields(builder, userId, issue);
        builder = buildMetadataFields(builder, issue, now);
        return builder.build();
    }

    private IssueDocument.IssueDocumentBuilder buildBasicFields(
        IssueDocument.IssueDocumentBuilder builder,
        Long userId,
        IssueNode issue) {
        return builder
            .userId(userId)
            .issueNumber(issue.getNumber())
            .title(issue.getTitle())
            .state(issue.getState())
            .repositoryName(issue.getRepoName())
            .repositoryOwner(issue.getRepoOwner());
    }

    private IssueDocument.IssueDocumentBuilder buildMetadataFields(
        IssueDocument.IssueDocumentBuilder builder,
        IssueNode issue,
        Instant now) {
        return builder
            .commentsCount(issue.getCommentsCount())
            .createdAt(issue.getCreatedAt())
            .closedAt(issue.getClosedAt())
            .collectedAt(now);
    }
}
