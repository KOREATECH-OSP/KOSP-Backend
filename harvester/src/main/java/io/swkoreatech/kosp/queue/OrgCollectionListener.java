package io.swkoreatech.kosp.queue;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swkoreatech.kosp.collection.entity.GithubRepositoryStatistics;
import io.swkoreatech.kosp.collection.repository.GithubRepositoryStatisticsRepository;
import io.swkoreatech.kosp.common.event.GithubOrgCollectionRequest;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.rabbitmq.client.Channel;

/**
 * RabbitMQ에서 GitHub 조직 레포지토리 수집 요청 메시지를 수신하는 리스너.
 *
 * <p>조직 등록 완료 후 발행된 메시지를 처리하여
 * 해당 조직의 GitHub 레포지토리 통계를 수집하고 DB에 저장한다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrgCollectionListener {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String GITHUB_API_VERSION = "2022-11-28";
    private static final int PER_PAGE = 100;

    private final UserRepository userRepository;
    private final TextEncryptor textEncryptor;
    private final GithubRepositoryStatisticsRepository repoStatsRepository;

    /**
     * GitHub 조직 레포지토리 수집 요청 메시지를 처리한다.
     *
     * @param request     수집 요청 DTO
     * @param deliveryTag 메시지 전달 태그
     * @param channel     RabbitMQ 채널
     * @throws IOException 채널 ACK/NACK 시 발생 가능
     */
    @RabbitListener(queues = QueueNames.GITHUB_ORG_COLLECTION)
    public void handleOrgCollectionRequest(
        GithubOrgCollectionRequest request,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
        Channel channel
    ) throws IOException {
        try {
            User user = userRepository.findById(request.adminUserId()).orElse(null);
            if (user == null || user.isDeleted() || !user.hasGithubUser()) {
                log.warn("Skipping org collection: admin user {} not found or has no GitHub account",
                    request.adminUserId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            String token = textEncryptor.decrypt(user.getGithubUser().getGithubToken());
            List<OrgRepoInfo> repos = fetchOrgRepos(token, request.orgName());
            String orgGithubId = String.valueOf(request.githubOrgId());

            for (OrgRepoInfo repo : repos) {
                saveOrgRepoStats(repo, orgGithubId);
            }

            log.info("Org repo collection complete for org {}: {} repos saved", request.orgName(), repos.size());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed org repo collection for org {}: {}", request.orgName(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private List<OrgRepoInfo> fetchOrgRepos(String token, String orgName) {
        WebClient client = WebClient.builder()
            .baseUrl(GITHUB_API_BASE)
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .defaultHeader("X-GitHub-Api-Version", GITHUB_API_VERSION)
            .build();

        List<OrgRepoInfo> all = new ArrayList<>();
        int page = 1;

        while (true) {
            String uri = String.format("/orgs/%s/repos?per_page=%d&type=all&page=%d", orgName, PER_PAGE, page);
            List<OrgRepoInfo> batch = client.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<OrgRepoInfo>>() {})
                .doOnError(e -> log.warn("GitHub 조직 레포 조회 실패 [{}] page {}: {}", orgName, page, e.getMessage()))
                .onErrorReturn(List.of())
                .block();

            if (batch == null || batch.isEmpty()) {
                break;
            }
            all.addAll(batch);
            if (batch.size() < PER_PAGE) {
                break;
            }
            page++;
        }

        return all;
    }

    private void saveOrgRepoStats(OrgRepoInfo repo, String orgGithubId) {
        if (repo.fullName() == null) {
            return;
        }
        String[] parts = repo.fullName().split("/", 2);
        if (parts.length < 2) {
            return;
        }
        String repoOwner = parts[0];
        String repoName = parts[1];

        GithubRepositoryStatistics stats = repoStatsRepository
            .findByRepoOwnerAndRepoNameAndContributorGithubId(repoOwner, repoName, orgGithubId)
            .orElse(GithubRepositoryStatistics.builder()
                .repoOwner(repoOwner)
                .repoName(repoName)
                .contributorGithubId(orgGithubId)
                .build());

        stats.updateRepositoryInfo(
            repo.stargazersCount() != null ? repo.stargazersCount() : 0,
            repo.forksCount() != null ? repo.forksCount() : 0,
            0,
            repo.description(),
            repo.language(),
            null
        );
        stats.updateOwnership(true);
        stats.updateUserContributions(0, 0, 0, parsePushedAt(repo.pushedAt()));
        stats.updateTotalCounts(0, 0, 0);

        repoStatsRepository.save(stats);
    }

    private LocalDateTime parsePushedAt(String pushedAt) {
        if (pushedAt == null) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(pushedAt), ZoneOffset.UTC);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * GitHub 조직 레포지토리 응답 매핑 DTO.
     */
    public record OrgRepoInfo(
        Long id,
        String name,
        @JsonProperty("full_name") String fullName,
        String description,
        String language,
        @JsonProperty("stargazers_count") Integer stargazersCount,
        @JsonProperty("forks_count") Integer forksCount,
        @JsonProperty("pushed_at") String pushedAt
    ) {}
}
