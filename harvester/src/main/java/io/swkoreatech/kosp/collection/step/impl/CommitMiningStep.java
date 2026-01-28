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
import io.swkoreatech.kosp.client.dto.RepositoryCommitsResponse;
import io.swkoreatech.kosp.client.dto.RepositoryCommitsResponse.CommitNode;
import io.swkoreatech.kosp.client.dto.RepositoryCommitsResponse.PageInfo;
import io.swkoreatech.kosp.collection.document.CommitDocument;
import io.swkoreatech.kosp.collection.repository.CommitDocumentRepository;
import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.collection.util.GraphQLErrorHandler;
import io.swkoreatech.kosp.collection.util.GraphQLTypeFactory;
import io.swkoreatech.kosp.collection.util.PaginationHelper;
import io.swkoreatech.kosp.collection.util.StepContextHelper;
import io.swkoreatech.kosp.job.StepCompletionListener;
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
        Instant now = Instant.now();
        return PaginationHelper.paginate(
            cursor -> fetchCommitsPage(owner, name, nodeId, cursor, token),
            RepositoryCommitsResponse::getPageInfo,
            (data, cursor) -> saveCommits(userId, owner, name, data.getCommits(), now),
            "repo",
            owner + "/" + name,
            RepositoryCommitsResponse.class
        );
    }




     private GraphQLResponse<RepositoryCommitsResponse> fetchCommitsPage(
         String owner,
         String name,
         String nodeId,
         String cursor,
         String token
     ) {
         return graphQLClient.getRepositoryCommits(owner, name, nodeId, cursor, token, GraphQLTypeFactory.<RepositoryCommitsResponse>responseType()).block();
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
        CommitDocument.CommitDocumentBuilder builder = CommitDocument.builder();
        builder = buildBasicFields(builder, userId, owner, name, commit);
        builder = buildAuthorFields(builder, commit);
        builder = buildStatisticsFields(builder, commit, now);
        return builder.build();
    }

    private CommitDocument.CommitDocumentBuilder buildBasicFields(
        CommitDocument.CommitDocumentBuilder builder,
        Long userId,
        String owner,
        String name,
        CommitNode commit
    ) {
        return builder
            .userId(userId)
            .sha(commit.getOid())
            .message(commit.getMessage())
            .repositoryName(name)
            .repositoryOwner(owner);
    }

    private CommitDocument.CommitDocumentBuilder buildAuthorFields(
        CommitDocument.CommitDocumentBuilder builder,
        CommitNode commit
    ) {
        return builder
            .authorName(commit.getAuthorName())
            .authorEmail(commit.getAuthorEmail())
            .authoredAt(commit.getAuthoredDate());
    }

    private CommitDocument.CommitDocumentBuilder buildStatisticsFields(
        CommitDocument.CommitDocumentBuilder builder,
        CommitNode commit,
        Instant now
    ) {
        return builder
            .additions(commit.getAdditions())
            .deletions(commit.getDeletions())
            .changedFiles(commit.getChangedFiles())
            .collectedAt(now);
    }
}
