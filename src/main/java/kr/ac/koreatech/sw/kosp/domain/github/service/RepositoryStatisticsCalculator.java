package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityNotFoundException;
import kr.ac.koreatech.sw.kosp.domain.github.client.graphql.GithubGraphQLClient;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubIssuesRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubPRsRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepositoryStatisticsCalculator {

    private final GithubCommitDetailRawRepository commitDetailRawRepository;
    private final GithubIssuesRawRepository issuesRawRepository;
    private final GithubPRsRawRepository prsRawRepository;
    private final GithubRepositoryStatisticsRepository repositoryStatisticsRepository;
    private final GithubGraphQLClient graphQLClient;
    private final GithubUserRepository githubUserRepository;
    private final TextEncryptor textEncryptor;

    public List<GithubRepositoryStatistics> calculate(String githubId) {
        log.info("Calculating repository statistics for user: {}", githubId);

        // 1. MongoDB에서 사용자의 모든 커밋 조회
        List<GithubCommitDetailRaw> commits = commitDetailRawRepository.findByAuthorLogin(githubId);

        if (commits.isEmpty()) {
            log.warn("No commits found for user: {}", githubId);
            return List.of();
        }

        // 2. 저장소별로 그룹화
        Map<String, List<GithubCommitDetailRaw>> commitsByRepo = commits.stream()
            .collect(Collectors.groupingBy(
                c -> c.getRepoOwner() + "/" + c.getRepoName()
            ));

        log.info("Found {} repositories for user: {}", commitsByRepo.size(), githubId);

        // 3. 각 저장소별 통계 계산
        List<GithubRepositoryStatistics> stats = new ArrayList<>();

        for (Map.Entry<String, List<GithubCommitDetailRaw>> entry : commitsByRepo.entrySet()) {
            try {
                GithubRepositoryStatistics stat = calculateRepositoryStatistics(
                    githubId,
                    entry.getKey(),
                    entry.getValue()
                );
                stats.add(stat);
            } catch (Exception e) {
                log.error("Failed to calculate statistics for repository: {}", entry.getKey(), e);
            }
        }

        // 4. 저장
        return repositoryStatisticsRepository.saveAll(stats);
    }

    private GithubRepositoryStatistics calculateRepositoryStatistics(
        String githubId,
        String repoFullName,
        List<GithubCommitDetailRaw> repoCommits
    ) {
        String[] parts = repoFullName.split("/");
        String owner = parts[0];
        String name = parts[1];

        // 기존 통계 조회 또는 새로 생성
        GithubRepositoryStatistics stat = repositoryStatisticsRepository
            .findByRepoOwnerAndRepoNameAndContributorGithubId(owner, name, githubId)
            .orElseGet(() -> GithubRepositoryStatistics.create(owner, name, githubId));

        // 사용자 커밋 수
        int userCommitsCount = repoCommits.size();

        // 최근 커밋일자
        LocalDateTime lastCommitDate = repoCommits.stream()
            .map(c -> {
                Map<String, Object> author = c.getAuthor();
                if (author != null && author.containsKey("date")) {
                    String dateStr = author.get("date").toString();
                    return LocalDateTime.parse(dateStr);
                }
                return null;
            })
            .filter(date -> date != null)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        // PR/Issue 수 집계
        int userPrsCount = countUserPRs(owner, name, githubId);
        int userIssuesCount = countUserIssues(owner, name, githubId);

        // 사용자 기여도 업데이트
        stat.updateUserContributions(
            userCommitsCount,
            userPrsCount,
            userIssuesCount,
            lastCommitDate
        );

        // GitHub API로 저장소 정보 조회
        try {
            // 1. GithubUser 조회
            GithubUser githubUser = githubUserRepository.findByGithubLogin(githubId)
                .orElseThrow(() -> new EntityNotFoundException("GitHub user not found: " + githubId));
            
            // 2. Token 복호화
            String token = textEncryptor.decrypt(githubUser.getGithubToken());
            
            // 3. GraphQL 호출
            Map<String, Object> response = graphQLClient.getRepositoryInfo(
                owner,
                name,
                token,
                Map.class
            ).block();

            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                Map<String, Object> repository = (Map<String, Object>) data.get("repository");

                if (repository != null) {
                    Integer stargazersCount = (Integer) repository.get("stargazerCount");
                    Integer forksCount = (Integer) repository.get("forkCount");
                    
                    Map<String, Object> watchers = (Map<String, Object>) repository.get("watchers");
                    Integer watchersCount = watchers != null ? (Integer) watchers.get("totalCount") : 0;
                    
                    String description = (String) repository.get("description");
                    
                    Map<String, Object> primaryLanguage = (Map<String, Object>) repository.get("primaryLanguage");
                    String language = primaryLanguage != null ? (String) primaryLanguage.get("name") : null;

                    // 저장소 생성일 추출
                    String createdAtStr = (String) repository.get("createdAt");
                    LocalDateTime createdAt = createdAtStr != null ? LocalDateTime.parse(createdAtStr) : null;

                    stat.updateRepositoryInfo(
                        stargazersCount != null ? stargazersCount : 0,
                        forksCount != null ? forksCount : 0,
                        watchersCount != null ? watchersCount : 0,
                        description,
                        language,
                        createdAt
                    );
                    
                    // 소유권 확인 (레포지토리 Owner와 Github ID가 일치하는지)
                    boolean isOwned = owner.equalsIgnoreCase(githubId);
                    stat.updateOwnership(isOwned);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch repository info for {}/{}: {}", owner, name, e.getMessage());
            // 실패 시 기본값 설정
            stat.updateRepositoryInfo(0, 0, 0, null, null, null);
        }

        return stat;
    }

    private int countUserPRs(String owner, String name, String githubId) {
        return prsRawRepository.findByRepoOwnerAndRepoName(owner, name)
            .stream()
            .flatMap(raw -> raw.getPullRequests().stream())
            .filter(pr -> {
                Map<String, Object> user = (Map<String, Object>) pr.get("user");
                return user != null && githubId.equals(user.get("login"));
            })
            .mapToInt(pr -> 1)
            .sum();
    }

    private int countUserIssues(String owner, String name, String githubId) {
        return issuesRawRepository.findByRepoOwnerAndRepoName(owner, name)
            .stream()
            .flatMap(raw -> raw.getIssues().stream())
            .filter(issue -> {
                Map<String, Object> user = (Map<String, Object>) issue.get("user");
                return user != null && githubId.equals(user.get("login"));
            })
            .mapToInt(issue -> 1)
            .sum();
    }
}
