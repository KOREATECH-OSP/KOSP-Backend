package io.swkoreatech.kosp.domain.admin.season.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.event.GithubCollectionRequest;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonRankingScore;
import io.swkoreatech.kosp.common.season.repository.SeasonRankingScoreRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.admin.season.dto.response.CollectionStatusDetailResponse;
import io.swkoreatech.kosp.domain.admin.season.dto.response.CollectionStatusDetailResponse.RepositoryCommitStat;
import io.swkoreatech.kosp.domain.admin.season.dto.response.CollectionStatusListResponse;
import io.swkoreatech.kosp.domain.admin.season.dto.response.CollectionStatusResponse;
import io.swkoreatech.kosp.domain.admin.season.dto.response.CollectionStatusResponse.CollectionStatus;
import io.swkoreatech.kosp.domain.season.mongo.SeasonCommitDocument;
import io.swkoreatech.kosp.domain.season.mongo.SeasonCommitRepository;
import io.swkoreatech.kosp.infra.rabbitmq.constants.QueueNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자용 수집 현황 조회 및 강제 수집 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCollectionService {

    private static final int ANOMALY_DAYS_THRESHOLD = 2;
    private static final int COMMIT_MIN_CHANGED_LINES = 5;

    private final SeasonRepository seasonRepository;
    private final SeasonRankingScoreRepository seasonRankingScoreRepository;
    private final SeasonCommitRepository seasonCommitRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 시즌별 전체 유저 수집 현황을 조회한다.
     */
    @Transactional(readOnly = true)
    public CollectionStatusListResponse getCollectionStatus(Long seasonId) {
        Season season = seasonRepository.getById(seasonId);
        Instant seasonStart = season.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant seasonEnd = season.getEndDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<SeasonRankingScore> scores = seasonRankingScoreRepository.findAllBySeason(season);

        List<CollectionStatusResponse> responses = scores.stream()
            .map(score -> toResponse(score, seasonStart, seasonEnd))
            .toList();

        return new CollectionStatusListResponse(responses);
    }

    /**
     * 특정 유저의 시즌 내 커밋 수집 현황을 레포지토리 단위로 조회한다.
     */
    @Transactional(readOnly = true)
    public CollectionStatusDetailResponse getCollectionStatusDetail(Long seasonId, Long userId) {
        Season season = seasonRepository.getById(seasonId);
        Instant seasonStart = season.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant seasonEnd = season.getEndDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        SeasonRankingScore score = seasonRankingScoreRepository.findAllBySeason(season).stream()
            .filter(s -> s.getUser().getId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("해당 시즌에 등록된 유저가 아닙니다."));

        User user = score.getUser();
        GithubUser githubUser = user.getGithubUser();

        String githubLogin = githubUser != null ? githubUser.getGithubLogin() : null;
        LocalDateTime lastCrawling = githubUser != null ? githubUser.getLastCrawling() : null;

        if (githubUser == null) {
            return new CollectionStatusDetailResponse(
                userId, user.getName(), null, null,
                CollectionStatus.NOT_COLLECTED, 0L, 0L, List.of()
            );
        }

        List<SeasonCommitDocument> commits = seasonCommitRepository
            .findByUserIdAndAuthoredAtBetween(userId, seasonStart, seasonEnd);

        Map<String, List<SeasonCommitDocument>> byRepo = commits.stream()
            .collect(Collectors.groupingBy(SeasonCommitDocument::getRepositoryName));

        List<RepositoryCommitStat> repoStats = byRepo.entrySet().stream()
            .map(entry -> {
                List<SeasonCommitDocument> repoCommits = entry.getValue();
                long total = repoCommits.size();
                long scored = repoCommits.stream()
                    .filter(c -> c.getAdditions() != null && c.getDeletions() != null
                        && (c.getAdditions() + c.getDeletions()) >= COMMIT_MIN_CHANGED_LINES)
                    .count();
                return new RepositoryCommitStat(entry.getKey(), total, scored);
            })
            .sorted(Comparator.comparingLong(RepositoryCommitStat::totalCommitCount).reversed())
            .toList();

        long totalCommitCount = commits.size();
        long scoredCommitCount = repoStats.stream().mapToLong(RepositoryCommitStat::scoredCommitCount).sum();

        return new CollectionStatusDetailResponse(
            userId, user.getName(), githubLogin, lastCrawling,
            determineStatus(lastCrawling), totalCommitCount, scoredCommitCount, repoStats
        );
    }

    /**
     * 특정 유저의 GitHub 데이터 수집을 즉시 요청한다.
     */
    public void forceCollect(Long userId) {
        sendCollectionRequest(userId);
        log.info("Force collection requested for user {}", userId);
    }

    /**
     * 시즌에 등록된 전체 유저의 GitHub 데이터 수집을 즉시 요청한다.
     */
    @Transactional(readOnly = true)
    public int forceCollectAll(Long seasonId) {
        Season season = seasonRepository.getById(seasonId);
        List<SeasonRankingScore> scores = seasonRankingScoreRepository.findAllBySeason(season);

        List<Long> userIds = scores.stream()
            .map(score -> score.getUser().getId())
            .toList();

        userIds.forEach(this::sendCollectionRequest);
        log.info("Force collection requested for all {} users in season {}", userIds.size(), seasonId);
        return userIds.size();
    }

    private void sendCollectionRequest(Long userId) {
        GithubCollectionRequest request = new GithubCollectionRequest(userId);
        rabbitTemplate.convertAndSend(
            QueueNames.GITHUB_COLLECTION_EXCHANGE,
            QueueNames.GITHUB_COLLECTION,
            request,
            message -> {
                message.getMessageProperties().setHeader("x-delay", 0);
                return message;
            }
        );
    }

    private CollectionStatusResponse toResponse(SeasonRankingScore score, Instant seasonStart, Instant seasonEnd) {
        User user = score.getUser();
        GithubUser githubUser = user.getGithubUser();

        String githubLogin = githubUser != null ? githubUser.getGithubLogin() : null;
        LocalDateTime lastCrawling = githubUser != null ? githubUser.getLastCrawling() : null;

        long commitCount = 0;
        if (githubUser != null) {
            commitCount = seasonCommitRepository
                .findByUserIdAndAuthoredAtBetween(user.getId(), seasonStart, seasonEnd)
                .size();
        }

        return new CollectionStatusResponse(
            user.getId(),
            user.getName(),
            githubLogin,
            lastCrawling,
            commitCount,
            determineStatus(lastCrawling)
        );
    }

    private CollectionStatus determineStatus(LocalDateTime lastCrawling) {
        if (lastCrawling == null) {
            return CollectionStatus.NOT_COLLECTED;
        }
        if (lastCrawling.isBefore(LocalDateTime.now().minusDays(ANOMALY_DAYS_THRESHOLD))) {
            return CollectionStatus.ANOMALY;
        }
        return CollectionStatus.NORMAL;
    }
}
