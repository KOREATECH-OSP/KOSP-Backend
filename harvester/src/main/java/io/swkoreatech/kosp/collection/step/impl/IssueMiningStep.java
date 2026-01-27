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
import io.swkoreatech.kosp.client.dto.UserIssuesResponse.PageInfo;
import io.swkoreatech.kosp.collection.document.IssueDocument;
import io.swkoreatech.kosp.collection.repository.IssueDocumentRepository;
import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.collection.util.GraphQLErrorHandler;
import io.swkoreatech.kosp.collection.util.GraphQLTypeFactory;
import io.swkoreatech.kosp.collection.util.StepContextHelper;
import io.swkoreatech.kosp.job.StepCompletionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        int totalIssues = fetchAllIssues(userId, login, token);
        log.info("Mined {} issues for user {}", totalIssues, userId);
    }

    private int fetchAllIssues(Long userId, String login, String token) {
        int saved = 0;
        String cursor = null;
        Instant now = Instant.now();

        do {
            GraphQLResponse<UserIssuesResponse> response = fetchIssuesPage(login, cursor, token);
            if (GraphQLErrorHandler.logAndCheckErrors(response, "user", login)) {
                break;
            }

            UserIssuesResponse data = response.getDataAs(UserIssuesResponse.class);
            List<IssueNode> issues = data.getIssues();
            saved += saveIssues(userId, issues, now);

            PageInfo pageInfo = data.getPageInfo();
            if (pageInfo == null || !pageInfo.isHasNextPage()) {
                break;
            }
            cursor = pageInfo.getEndCursor();
        } while (cursor != null);

        return saved;
    }

    private GraphQLResponse<UserIssuesResponse> fetchIssuesPage(String login, String cursor, String token) {
        return graphQLClient.getUserIssues(login, cursor, token, GraphQLTypeFactory.<UserIssuesResponse>responseType()).block();
    }

    private int saveIssues(Long userId, List<IssueNode> issues, Instant now) {
        int saved = 0;
        for (IssueNode issue : issues) {
            if (issueDocumentRepository.existsByUserIdAndIssueNumber(userId, issue.getNumber())) {
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
