package io.swkoreatech.kosp.collection.step.impl;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.client.GithubGraphQLClient;
import io.swkoreatech.kosp.client.dto.ContributedReposResponse;
import io.swkoreatech.kosp.client.dto.ContributedReposResponse.RepositoryInfo;
import io.swkoreatech.kosp.client.dto.GraphQLResponse;
import io.swkoreatech.kosp.collection.document.ContributedRepoDocument;
import io.swkoreatech.kosp.collection.repository.ContributedRepoDocumentRepository;
import io.swkoreatech.kosp.collection.step.StepProvider;
import io.swkoreatech.kosp.collection.util.GraphQLErrorHandler;
import io.swkoreatech.kosp.collection.util.GraphQLTypeFactory;
import io.swkoreatech.kosp.collection.util.StepContextHelper;
import io.swkoreatech.kosp.job.StepCompletionListener;
import io.swkoreatech.kosp.user.User;
import io.swkoreatech.kosp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepositoryDiscoveryStep implements StepProvider {

    private static final String STEP_NAME = "repositoryDiscoveryStep";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final GithubGraphQLClient graphQLClient;
    private final TextEncryptor textEncryptor;
    private final ContributedRepoDocumentRepository repoDocumentRepository;
    private final StepCompletionListener stepCompletionListener;

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

    @Override
    public String getStepName() {
        return STEP_NAME;
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

        GraphQLResponse<ContributedReposResponse> response = fetchContributedRepos(login, token);
        if (GraphQLErrorHandler.logAndCheckErrors(response, "user", login)) {
            return;
        }

        ContributedReposResponse data = response.getDataAs(ContributedReposResponse.class);
        Set<RepositoryInfo> repositories = data.collectAllRepositories();
        String userNodeId = data.getUserNodeId();

        saveRepositories(userId, login, repositories);
        storeUserInfoInContext(chunkContext, login, token, userNodeId);
        storeReposInContext(chunkContext, repositories);
        log.info("Discovered {} repositories for user {}", repositories.size(), userId);
    }

    private String decryptToken(String encryptedToken) {
        return textEncryptor.decrypt(encryptedToken);
    }

    private Set<RepositoryInfo> discoverRepositories(String login, String token) {
        GraphQLResponse<ContributedReposResponse> response = fetchContributedRepos(login, token);
        if (response == null || response.hasErrors()) {
            return Set.of();
        }
        return response.getDataAs(ContributedReposResponse.class).collectAllRepositories();
    }

    private GraphQLResponse<ContributedReposResponse> fetchContributedRepos(String login, String token) {
        String[] timeRange = calculateTimeRange();
        return graphQLClient
            .getContributedRepos(login, timeRange[0], timeRange[1], token, GraphQLTypeFactory.responseType())
            .block();
    }

    private String[] calculateTimeRange() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime oneYearAgo = now.minusYears(1);
        return new String[] { formatDateTime(oneYearAgo), formatDateTime(now) };
    }

    private String formatDateTime(ZonedDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_INSTANT);
    }

    private void saveRepositories(Long userId, String login, Set<RepositoryInfo> repositories) {
        Instant now = Instant.now();
        for (RepositoryInfo repo : repositories) {
            ContributedRepoDocument document = buildRepoDocument(userId, login, repo, now);
            repoDocumentRepository.save(document);
        }
    }

    private ContributedRepoDocument buildRepoDocument(Long userId, String login, RepositoryInfo repo, Instant now) {
        var builder = ContributedRepoDocument.builder()
            .userId(userId)
            .repositoryName(repo.getName())
            .repositoryOwner(repo.getOwnerLogin())
            .fullName(repo.getNameWithOwner())
            .description(repo.getDescription());
        return buildRepoMetadata(builder, login, repo, now).build();
    }

    private ContributedRepoDocument.ContributedRepoDocumentBuilder buildRepoMetadata(
            ContributedRepoDocument.ContributedRepoDocumentBuilder builder,
            String login,
            RepositoryInfo repo,
            Instant now) {
        return builder
            .isOwner(login.equals(repo.getOwnerLogin()))
            .isFork(repo.isFork())
            .isPrivate(repo.isPrivate())
            .primaryLanguage(repo.getLanguageName())
            .stargazersCount(repo.getStargazerCount())
            .forksCount(repo.getForkCount())
            .collectedAt(now);
    }

    private void storeUserInfoInContext(ChunkContext chunkContext, String login, String token, String nodeId) {
        var context = chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getExecutionContext();

        context.putString("githubLogin", login);
        context.putString("githubToken", token);
        context.putString("githubNodeId", nodeId);
    }

    private void storeReposInContext(ChunkContext chunkContext, Set<RepositoryInfo> repositories) {
        String[] repoFullNames = repositories.stream()
            .map(RepositoryInfo::getNameWithOwner)
            .toArray(String[]::new);

        chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getExecutionContext()
            .put("discoveredRepos", repoFullNames);
    }
}
