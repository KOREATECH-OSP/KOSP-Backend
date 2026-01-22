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
import io.swkoreatech.kosp.harvester.client.dto.PullRequestListItem;
import io.swkoreatech.kosp.harvester.client.dto.PullRequestResponse;
import io.swkoreatech.kosp.harvester.collection.document.PullRequestDocument;
import io.swkoreatech.kosp.harvester.collection.repository.PullRequestDocumentRepository;
import io.swkoreatech.kosp.harvester.collection.step.StepProvider;
import io.swkoreatech.kosp.harvester.user.GithubUser;
import io.swkoreatech.kosp.harvester.user.User;
import io.swkoreatech.kosp.harvester.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PullRequestMiningStep implements StepProvider {

    private static final String STEP_NAME = "pullRequestMiningStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final GithubRestApiClient restApiClient;
    private final TextEncryptor textEncryptor;
    private final PullRequestDocumentRepository prDocumentRepository;

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
            log.info("No repositories to mine PRs from for user {}", userId);
            return;
        }

        int totalPrs = 0;
        for (String repoFullName : repos) {
            int prs = minePullRequestsForRepo(userId, login, repoFullName, token);
            totalPrs += prs;
        }

        log.info("Mined {} PRs across {} repos for user {}", totalPrs, repos.length, userId);
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

    private int minePullRequestsForRepo(Long userId, String login, String repoFullName, String token) {
        String[] parts = repoFullName.split("/");
        if (parts.length != 2) {
            log.warn("Invalid repo name format: {}", repoFullName);
            return 0;
        }

        String owner = parts[0];
        String repoName = parts[1];
        String uri = buildPullRequestsUri(owner, repoName, login);

        List<PullRequestListItem> prs = fetchPullRequestList(uri, token);
        if (prs == null || prs.isEmpty()) {
            return 0;
        }

        return processPullRequests(userId, owner, repoName, prs, token);
    }

    private String buildPullRequestsUri(String owner, String repo, String author) {
        return String.format("/repos/%s/%s/pulls?state=all&creator=%s", owner, repo, author);
    }

    private List<PullRequestListItem> fetchPullRequestList(String uri, String token) {
        return restApiClient.getAllWithPagination(uri, token, PullRequestListItem.class).block();
    }

    private int processPullRequests(Long userId, String owner, String repoName, List<PullRequestListItem> prs, String token) {
        Instant now = Instant.now();
        int saved = 0;

        for (PullRequestListItem item : prs) {
            if (prDocumentRepository.existsByUserIdAndPrNumber(userId, item.getNumber())) {
                continue;
            }

            PullRequestResponse detail = fetchPullRequestDetail(owner, repoName, item.getNumber(), token);
            if (detail == null) {
                continue;
            }

            PullRequestDocument document = buildDocument(userId, owner, repoName, detail, now);
            prDocumentRepository.save(document);
            saved++;
        }

        return saved;
    }

    private PullRequestResponse fetchPullRequestDetail(String owner, String repo, Long prNumber, String token) {
        String uri = String.format("/repos/%s/%s/pulls/%d", owner, repo, prNumber);
        return restApiClient.get(uri, token, PullRequestResponse.class).block();
    }

    private PullRequestDocument buildDocument(Long userId, String owner, String repoName, PullRequestResponse detail, Instant now) {
        return PullRequestDocument.builder()
            .userId(userId)
            .prNumber(detail.getNumber())
            .title(detail.getTitle())
            .state(detail.getState())
            .repositoryName(repoName)
            .repositoryOwner(owner)
            .additions(detail.getAdditions())
            .deletions(detail.getDeletions())
            .changedFiles(detail.getChangedFiles())
            .commitsCount(detail.getCommits())
            .merged(detail.isMerged())
            .mergedAt(detail.getMergedAt())
            .createdAt(detail.getCreatedAt())
            .closedAt(detail.getClosedAt())
            .collectedAt(now)
            .build();
    }
}
