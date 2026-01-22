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
import io.swkoreatech.kosp.harvester.client.dto.RepositoryCommitsResponse;
import io.swkoreatech.kosp.harvester.client.dto.RepositoryCommitsResponse.CommitNode;
import io.swkoreatech.kosp.harvester.client.dto.RepositoryCommitsResponse.PageInfo;
import io.swkoreatech.kosp.harvester.collection.document.CommitDocument;
import io.swkoreatech.kosp.harvester.collection.repository.CommitDocumentRepository;
import io.swkoreatech.kosp.harvester.collection.step.StepProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommitMiningStep implements StepProvider {

    private static final String STEP_NAME = "commitMiningStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GithubGraphQLClient graphQLClient;
    private final CommitDocumentRepository commitDocumentRepository;

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
        String token = context.getString("githubToken");
        String nodeId = context.getString("githubNodeId");

        if (token == null || nodeId == null) {
            log.warn("GitHub credentials not found in context for user {}", userId);
            return;
        }

        String[] repos = getDiscoveredRepos(context);
        if (repos == null || repos.length == 0) {
            log.info("No repositories to mine commits from for user {}", userId);
            return;
        }

        int totalCommits = mineCommitsFromRepos(userId, repos, nodeId, token);
        log.info("Mined {} commits across {} repos for user {}", totalCommits, repos.length, userId);
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

    private String[] getDiscoveredRepos(ExecutionContext context) {
        Object repos = context.get("discoveredRepos");
        if (repos instanceof String[]) {
            return (String[]) repos;
        }
        return null;
    }

    private int mineCommitsFromRepos(Long userId, String[] repos, String nodeId, String token) {
        int total = 0;
        for (String repoFullName : repos) {
            total += mineCommitsForRepo(userId, repoFullName, nodeId, token);
        }
        return total;
    }

    private int mineCommitsForRepo(Long userId, String repoFullName, String nodeId, String token) {
        String[] parts = repoFullName.split("/");
        if (parts.length != 2) {
            log.warn("Invalid repo name format: {}", repoFullName);
            return 0;
        }

        String owner = parts[0];
        String name = parts[1];
        return fetchAllCommits(userId, owner, name, nodeId, token);
    }

    private int fetchAllCommits(Long userId, String owner, String name, String nodeId, String token) {
        int saved = 0;
        String cursor = null;
        Instant now = Instant.now();

        do {
            GraphQLResponse<RepositoryCommitsResponse> response = fetchCommitsPage(owner, name, nodeId, cursor, token);
            if (response == null || response.hasErrors()) {
                logErrors(response, owner, name);
                break;
            }

            List<CommitNode> commits = response.getData().getCommits();
            saved += saveCommits(userId, owner, name, commits, now);

            PageInfo pageInfo = response.getData().getPageInfo();
            if (pageInfo == null || !pageInfo.isHasNextPage()) {
                break;
            }
            cursor = pageInfo.getEndCursor();
        } while (cursor != null);

        return saved;
    }

    private GraphQLResponse<RepositoryCommitsResponse> fetchCommitsPage(
        String owner,
        String name,
        String nodeId,
        String cursor,
        String token
    ) {
        return graphQLClient.getRepositoryCommits(owner, name, nodeId, cursor, token, createResponseType()).block();
    }

    @SuppressWarnings("unchecked")
    private Class<GraphQLResponse<RepositoryCommitsResponse>> createResponseType() {
        return (Class<GraphQLResponse<RepositoryCommitsResponse>>) (Class<?>) GraphQLResponse.class;
    }

    private void logErrors(GraphQLResponse<RepositoryCommitsResponse> response, String owner, String name) {
        if (response == null) {
            log.warn("No response from GraphQL for repo {}/{}", owner, name);
            return;
        }
        log.error("GraphQL errors for repo {}/{}: {}", owner, name, response.getErrors());
    }

    private int saveCommits(Long userId, String owner, String name, List<CommitNode> commits, Instant now) {
        int saved = 0;
        for (CommitNode commit : commits) {
            if (commitDocumentRepository.existsByUserIdAndSha(userId, commit.getOid())) {
                continue;
            }

            CommitDocument document = buildDocument(userId, owner, name, commit, now);
            commitDocumentRepository.save(document);
            saved++;
        }
        return saved;
    }

    private CommitDocument buildDocument(Long userId, String owner, String name, CommitNode commit, Instant now) {
        return CommitDocument.builder()
            .userId(userId)
            .sha(commit.getOid())
            .message(commit.getMessage())
            .repositoryName(name)
            .repositoryOwner(owner)
            .authorName(commit.getAuthorName())
            .authorEmail(commit.getAuthorEmail())
            .authoredAt(commit.getAuthoredDate())
            .additions(commit.getAdditions())
            .deletions(commit.getDeletions())
            .changedFiles(commit.getChangedFiles())
            .collectedAt(now)
            .build();
    }
}
