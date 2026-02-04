package io.swkoreatech.kosp.collection.step.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.client.GithubGraphQLClient;
import io.swkoreatech.kosp.client.dto.ContributedReposResponse.RepositoryInfo;
import io.swkoreatech.kosp.client.dto.GraphQLResponse;
import io.swkoreatech.kosp.client.dto.UserBasicInfoResponse;
import io.swkoreatech.kosp.collection.repository.CollectionMetadataRepository;
import io.swkoreatech.kosp.collection.repository.ContributedRepoDocumentRepository;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.job.ContextValidationListener;
import io.swkoreatech.kosp.job.StepCompletionListener;
import reactor.core.publisher.Mono;

@DisplayName("RepositoryDiscoveryStep 단위 테스트")
@ExtendWith(MockitoExtension.class)
class RepositoryDiscoveryStepTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GithubGraphQLClient githubGraphQLClient;

    @Mock
    private TextEncryptor textEncryptor;

    @Mock
    private ContributedRepoDocumentRepository repoDocumentRepository;

    @Mock
    private CollectionMetadataRepository metadataRepository;

    @Mock
    private StepCompletionListener stepCompletionListener;

    @Mock
    private ContextValidationListener contextValidationListener;

    @InjectMocks
    private RepositoryDiscoveryStep repositoryDiscoveryStep;

    @Nested
    @DisplayName("fetchOwnedRepositories 메서드")
    class FetchOwnedRepositoriesTest {

        @Test
        @DisplayName("기여 없는 본인 소유 레포도 수집되어야 함")
        void shouldCollectOwnedRepositoriesWithoutContributions() throws Exception {
            String login = "testuser";
            String token = "test-token";
            String ownedRepoName = "my-owned-repo";
            String ownedRepoFullName = "testuser/my-owned-repo";

            UserBasicInfoResponse mockResponse = createMockUserBasicInfoResponse(
                login, 
                ownedRepoName, 
                ownedRepoFullName,
                false
            );

            GraphQLResponse<UserBasicInfoResponse> graphQLResponse = new GraphQLResponse<>();
            setGraphQLResponseData(graphQLResponse, mockResponse);

            when(githubGraphQLClient.getUserBasicInfo(eq(login), any(), eq(token), any()))
                .thenReturn(Mono.just(graphQLResponse));

            Set<RepositoryInfo> result = invokeFetchOwnedRepositories(login, token);

            assertThat(result).isNotEmpty();
            assertThat(result).anyMatch(repo -> 
                repo.getName().equals(ownedRepoName) &&
                repo.getNameWithOwner().equals(ownedRepoFullName)
            );
        }

        @Test
        @DisplayName("페이지네이션을 통해 100개 이상 레포 수집")
        void shouldHandlePaginationForMoreThan100Repos() throws Exception {
            String login = "testuser";
            String token = "test-token";

            UserBasicInfoResponse firstPage = createMockUserBasicInfoResponse(
                login, 
                "repo-1", 
                "testuser/repo-1",
                true
            );
            UserBasicInfoResponse secondPage = createMockUserBasicInfoResponse(
                login, 
                "repo-2", 
                "testuser/repo-2",
                false
            );

            GraphQLResponse<UserBasicInfoResponse> firstResponse = new GraphQLResponse<>();
            setGraphQLResponseData(firstResponse, firstPage);
            
            GraphQLResponse<UserBasicInfoResponse> secondResponse = new GraphQLResponse<>();
            setGraphQLResponseData(secondResponse, secondPage);

            when(githubGraphQLClient.getUserBasicInfo(eq(login), eq(null), eq(token), any()))
                .thenReturn(Mono.just(firstResponse));
            when(githubGraphQLClient.getUserBasicInfo(eq(login), eq("cursor-1"), eq(token), any()))
                .thenReturn(Mono.just(secondResponse));

            Set<RepositoryInfo> result = invokeFetchOwnedRepositories(login, token);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("null 데이터 처리 시 빈 Set 반환")
        void shouldReturnEmptySet_whenDataIsNull() throws Exception {
            String login = "testuser";
            String token = "test-token";

            GraphQLResponse<UserBasicInfoResponse> graphQLResponse = new GraphQLResponse<>();
            setGraphQLResponseData(graphQLResponse, null);

            when(githubGraphQLClient.getUserBasicInfo(eq(login), any(), eq(token), any()))
                .thenReturn(Mono.just(graphQLResponse));

            Set<RepositoryInfo> result = invokeFetchOwnedRepositories(login, token);

            assertThat(result).isEmpty();
        }
    }

    private Set<RepositoryInfo> invokeFetchOwnedRepositories(String login, String token) throws Exception {
        Method method = RepositoryDiscoveryStep.class.getDeclaredMethod(
            "fetchOwnedRepositories", 
            String.class, 
            String.class
        );
        method.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        Set<RepositoryInfo> result = (Set<RepositoryInfo>) method.invoke(
            repositoryDiscoveryStep, 
            login, 
            token
        );
        return result;
    }

    private UserBasicInfoResponse createMockUserBasicInfoResponse(
        String login, 
        String repoName,
        String repoFullName,
        boolean hasNextPage
    ) {
        UserBasicInfoResponse response = new UserBasicInfoResponse();
        UserBasicInfoResponse.User user = new UserBasicInfoResponse.User();
        UserBasicInfoResponse.RepositoriesData reposData = new UserBasicInfoResponse.RepositoriesData();
        UserBasicInfoResponse.PageInfo pageInfo = new UserBasicInfoResponse.PageInfo();
        UserBasicInfoResponse.RepositoryNode repoNode = new UserBasicInfoResponse.RepositoryNode();
        UserBasicInfoResponse.Owner owner = new UserBasicInfoResponse.Owner();

        setField(owner, "login", login);
        setField(repoNode, "name", repoName);
        setField(repoNode, "nameWithOwner", repoFullName);
        setField(repoNode, "owner", owner);
        setField(repoNode, "isFork", false);
        setField(repoNode, "isPrivate", false);
        setField(repoNode, "stargazerCount", 10);
        setField(repoNode, "forkCount", 5);

        setField(pageInfo, "hasNextPage", hasNextPage);
        setField(pageInfo, "endCursor", hasNextPage ? "cursor-1" : null);

        setField(reposData, "nodes", List.of(repoNode));
        setField(reposData, "pageInfo", pageInfo);

        setField(user, "repositories", reposData);
        setField(response, "user", user);

        return response;
    }

    private void setGraphQLResponseData(GraphQLResponse<?> response, Object data) {
        try {
            java.lang.reflect.Field dataField = GraphQLResponse.class.getDeclaredField("data");
            dataField.setAccessible(true);
            dataField.set(response, data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set GraphQLResponse data", e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
