package io.swkoreatech.kosp.harvester.collection.step.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.harvester.client.GithubRestApiClient;
import io.swkoreatech.kosp.harvester.client.dto.CommitListItem;
import io.swkoreatech.kosp.harvester.client.dto.CommitResponse;
import io.swkoreatech.kosp.harvester.collection.document.CommitDocument;
import io.swkoreatech.kosp.harvester.collection.repository.CommitDocumentRepository;
import io.swkoreatech.kosp.harvester.collection.step.StepProvider;
import io.swkoreatech.kosp.harvester.user.GithubUser;
import io.swkoreatech.kosp.harvester.user.User;
import io.swkoreatech.kosp.harvester.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommitMiningStep implements StepProvider {

    private static final String STEP_NAME = "commitMiningStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final GithubRestApiClient restApiClient;
    private final TextEncryptor textEncryptor;
    private final CommitDocumentRepository commitDocumentRepository;

    @Override
    public Step getStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .tasklet((contribution, chunkContext) -> {
                Long userId = extractUserId(chunkContext);
                execute(userId, chunkContext);
                return RepeatStatus.FINISHED;
            }, transactionManager)
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
        User user = userRepository.getById(userId);
        if (!user.hasGithubUser()) {
            log.warn("User {} does not have GitHub account linked", userId);
            return;
        }

        GithubUser githubUser = user.getGithubUser();
        String token = decryptToken(githubUser.getGithubToken());
        String login = githubUser.getGithubLogin();

        String[] repos = getDiscoveredRepos(chunkContext);
        if (repos == null || repos.length == 0) {
            log.info("No repositories to mine commits from for user {}", userId);
            return;
        }

        int totalCommits = 0;
        for (String repoFullName : repos) {
            int commits = mineCommitsForRepo(userId, login, repoFullName, token);
            totalCommits += commits;
        }

        log.info("Mined {} commits across {} repos for user {}", totalCommits, repos.length, userId);
    }

    private String decryptToken(String encryptedToken) {
        return textEncryptor.decrypt(encryptedToken);
    }

    private String[] getDiscoveredRepos(ChunkContext chunkContext) {
        Object repos = chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getExecutionContext()
            .get("discoveredRepos");

        if (repos instanceof String[]) {
            return (String[]) repos;
        }
        return null;
    }

    private int mineCommitsForRepo(Long userId, String login, String repoFullName, String token) {
        String[] parts = repoFullName.split("/");
        if (parts.length != 2) {
            log.warn("Invalid repo name format: {}", repoFullName);
            return 0;
        }

        String owner = parts[0];
        String repoName = parts[1];
        String uri = buildCommitsUri(owner, repoName, login);

        List<CommitListItem> commits = fetchCommitList(uri, token);
        if (commits == null || commits.isEmpty()) {
            return 0;
        }

        return processCommits(userId, owner, repoName, commits, token);
    }

    private String buildCommitsUri(String owner, String repo, String author) {
        return String.format("/repos/%s/%s/commits?author=%s", owner, repo, author);
    }

    private List<CommitListItem> fetchCommitList(String uri, String token) {
        return restApiClient.getAllWithPagination(uri, token, CommitListItem.class).block();
    }

    private int processCommits(Long userId, String owner, String repoName, List<CommitListItem> commits, String token) {
        Instant now = Instant.now();
        int saved = 0;

        for (CommitListItem item : commits) {
            if (commitDocumentRepository.existsByUserIdAndSha(userId, item.getSha())) {
                continue;
            }

            CommitResponse detail = fetchCommitDetail(owner, repoName, item.getSha(), token);
            if (detail == null) {
                continue;
            }

            CommitDocument document = buildDocument(userId, owner, repoName, detail, now);
            commitDocumentRepository.save(document);
            saved++;
        }

        return saved;
    }

    private CommitResponse fetchCommitDetail(String owner, String repo, String sha, String token) {
        String uri = String.format("/repos/%s/%s/commits/%s", owner, repo, sha);
        return restApiClient.get(uri, token, CommitResponse.class).block();
    }

    private CommitDocument buildDocument(Long userId, String owner, String repoName, CommitResponse detail, Instant now) {
        return CommitDocument.builder()
            .userId(userId)
            .sha(detail.getSha())
            .message(detail.getMessage())
            .repositoryName(repoName)
            .repositoryOwner(owner)
            .authorName(detail.getAuthorName())
            .authorEmail(detail.getAuthorEmail())
            .authoredAt(detail.getAuthoredAt())
            .additions(detail.getAdditions())
            .deletions(detail.getDeletions())
            .changedFiles(detail.getChangedFiles())
            .collectedAt(now)
            .build();
    }
}
