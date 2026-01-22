package io.swkoreatech.kosp.harvester.collection.step.impl;

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

import io.swkoreatech.kosp.harvester.client.GithubGraphQLClient;
import io.swkoreatech.kosp.harvester.client.dto.ContributedReposResponse;
import io.swkoreatech.kosp.harvester.client.dto.ContributedReposResponse.RepositoryInfo;
import io.swkoreatech.kosp.harvester.client.dto.GraphQLResponse;
import io.swkoreatech.kosp.harvester.collection.document.ContributedRepoDocument;
import io.swkoreatech.kosp.harvester.collection.repository.ContributedRepoDocumentRepository;
import io.swkoreatech.kosp.harvester.collection.step.StepProvider;
import io.swkoreatech.kosp.harvester.user.GithubUser;
import io.swkoreatech.kosp.harvester.user.User;
import io.swkoreatech.kosp.harvester.user.UserRepository;
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

        Set<RepositoryInfo> repositories = discoverRepositories(login, token);
        saveRepositories(userId, login, repositories);

        storeReposInContext(chunkContext, repositories);
        log.info("Discovered {} repositories for user {}", repositories.size(), userId);
    }

    private String decryptToken(String encryptedToken) {
        return textEncryptor.decrypt(encryptedToken);
    }

    private Set<RepositoryInfo> discoverRepositories(String login, String token) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime oneYearAgo = now.minusYears(1);

        String from = formatDateTime(oneYearAgo);
        String to = formatDateTime(now);

        GraphQLResponse<ContributedReposResponse> response = graphQLClient
            .getContributedRepos(login, from, to, token, createResponseType())
            .block();

        if (response == null) {
            return Set.of();
        }
        if (response.hasErrors()) {
            log.error("GraphQL errors: {}", response.getErrors());
            return Set.of();
        }

        return response.getData().collectAllRepositories();
    }

    private String formatDateTime(ZonedDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_INSTANT);
    }

    @SuppressWarnings("unchecked")
    private Class<GraphQLResponse<ContributedReposResponse>> createResponseType() {
        return (Class<GraphQLResponse<ContributedReposResponse>>) (Class<?>) GraphQLResponse.class;
    }

    private void saveRepositories(Long userId, String login, Set<RepositoryInfo> repositories) {
        Instant now = Instant.now();

        for (RepositoryInfo repo : repositories) {
            ContributedRepoDocument document = ContributedRepoDocument.builder()
                .userId(userId)
                .repositoryName(repo.getName())
                .repositoryOwner(repo.getOwnerLogin())
                .fullName(repo.getNameWithOwner())
                .isOwner(login.equals(repo.getOwnerLogin()))
                .isFork(repo.isFork())
                .isPrivate(repo.isPrivate())
                .primaryLanguage(repo.getLanguageName())
                .stargazersCount(repo.getStargazerCount())
                .forksCount(repo.getForkCount())
                .collectedAt(now)
                .build();

            repoDocumentRepository.save(document);
        }
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
