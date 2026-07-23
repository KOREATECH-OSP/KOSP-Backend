package io.swkoreatech.kosp.domain.season.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.challenge.repository.ChallengeHistoryRepository;
import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonRankingScore;
import io.swkoreatech.kosp.common.season.model.enums.SeasonTier;
import io.swkoreatech.kosp.common.season.repository.SeasonRankingScoreRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.season.mongo.SeasonCommitDocument;
import io.swkoreatech.kosp.domain.season.mongo.SeasonCommitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시즌 랭킹 배치 서비스.
 *
 * <p>매일 새벽 4시에 실행되며 다음 작업을 수행한다:</p>
 * <ol>
 *   <li>커밋 점수 재계산 (MongoDB CommitDocument 기반, 일별 cap 3건)</li>
 *   <li>챌린지 점수 재계산 (ChallengeHistory.createdAt 기반, createdAt으로 달성 시점 대용)</li>
 *   <li>총점 재계산 + 티어 갱신</li>
 *   <li>RANK() 방식으로 순위 갱신 (동점자 공동 순위)</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonRankingBatchService {

    private static final BigDecimal COMMIT_SCORE_PER_UNIT = new BigDecimal("0.0500");
    private static final int COMMIT_DAILY_CAP = 3;

    // 챌린지 레벨(tier)별 점수 단가
    private static final Map<Integer, BigDecimal> CHALLENGE_TIER_SCORE_MAP = Map.of(
        1, new BigDecimal("0.25"),
        2, new BigDecimal("0.40"),
        3, new BigDecimal("0.55"),
        4, new BigDecimal("0.70"),
        5, new BigDecimal("0.90"),
        6, new BigDecimal("1.10"),
        7, new BigDecimal("1.40"),
        8, new BigDecimal("1.80"),
        9, new BigDecimal("2.30"),
        10, new BigDecimal("2.80")
    );

    private final SeasonRepository seasonRepository;
    private final SeasonRankingScoreRepository rankingScoreRepository;
    private final UserRepository userRepository;
    private final SeasonCommitRepository seasonCommitRepository;
    private final ChallengeHistoryRepository challengeHistoryRepository;
    private final SeasonScoreService seasonScoreService;
    private final SeasonTierTitleService seasonTierTitleService;

    /**
     * 시즌 랭킹 배치를 실행한다. 매일 새벽 4시 실행.
     *
     * <p>cron 표현식: 초 분 시 일 월 요일</p>
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void runRankingBatch() {
        Optional<Season> activeSeason = seasonRepository.findByIsActiveTrue();
        if (activeSeason.isEmpty()) {
            log.info("[SeasonBatch] 활성 시즌 없음. 배치 스킵.");
            return;
        }

        Season season = activeSeason.get();
        log.info("[SeasonBatch] 시즌 랭킹 배치 시작. seasonId={}, name={}", season.getId(), season.getName());

        try {
            List<User> activeUsers = userRepository.findAllByIsDeletedFalse();
            log.info("[SeasonBatch] 대상 유저 수: {}", activeUsers.size());

            recalculateCommitAndChallengeScores(season, activeUsers);
            recalculateRanks(season);
            assignTiers(season);
            seasonTierTitleService.syncTierTitles(season);

            log.info("[SeasonBatch] 배치 완료. 처리 유저={}", activeUsers.size());
        } catch (Exception e) {
            log.error("[SeasonBatch] 배치 실행 중 오류 발생. seasonId={}", season.getId(), e);
        }
    }

    @Transactional
    public void recalculateCommitAndChallengeScores(Season season, List<User> users) {
        Instant seasonStart = season.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant now = Instant.now();
        long daysElapsed = ChronoUnit.DAYS.between(season.getStartDate(), LocalDate.now()) + 1;

        for (User user : users) {
            try {
                recalculateForUser(season, user, seasonStart, now, daysElapsed);
            } catch (Exception e) {
                log.warn("[SeasonBatch] 유저 처리 중 오류 스킵. userId={}, error={}", user.getId(), e.getMessage());
            }
        }
    }

    private void recalculateForUser(Season season, User user, Instant seasonStart, Instant now, long daysElapsed) {
        SeasonRankingScore score = seasonScoreService.getOrCreate(season, user);

        // 커밋 점수: MongoDB 기반 일별 cap 적용
        BigDecimal newCommitScore = calculateCommitScore(user.getId(), seasonStart, now, daysElapsed);
        score.updateCommitScore(newCommitScore);

        // 챌린지 점수: ChallengeHistory.createdAt 기반 (achievedAt 대용)
        BigDecimal newChallengeScore = calculateChallengeScore(user, season);
        // 챌린지는 add가 아닌 재계산이므로 reset 후 재적용
        resetAndSetChallengeScore(score, newChallengeScore);

        score.recalculate();
        rankingScoreRepository.save(score);
    }

    private BigDecimal calculateCommitScore(Long userId, Instant seasonStart, Instant now, long daysElapsed) {
        List<SeasonCommitDocument> commits =
            seasonCommitRepository.findByUserIdAndAuthoredAtBetween(userId, seasonStart, now);

        // 일별 커밋 수 집계 후 일별 cap 적용
        Map<LocalDate, Long> dailyCommitCounts = commits.stream()
            .filter(c -> !isBotCommit(c.getMessage()))
            .collect(Collectors.groupingBy(
                c -> c.getAuthoredAt().atZone(ZoneOffset.UTC).toLocalDate(),
                Collectors.counting()
            ));

        long cappedTotal = dailyCommitCounts.values().stream()
            .mapToLong(count -> Math.min(count, COMMIT_DAILY_CAP))
            .sum();

        return COMMIT_SCORE_PER_UNIT.multiply(BigDecimal.valueOf(cappedTotal))
            .setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateChallengeScore(User user, Season season) {
        var startDateTime = season.getStartDate().atStartOfDay();
        var endDateTime = season.getEndDate().plusDays(1).atStartOfDay();

        List<Object[]> tierCounts = challengeHistoryRepository
            .countAchievedGroupByTierInPeriod(user, startDateTime, endDateTime);

        BigDecimal total = BigDecimal.ZERO;
        for (Object[] row : tierCounts) {
            Integer tier = (Integer) row[0];
            Long count = (Long) row[1];
            BigDecimal unitScore = CHALLENGE_TIER_SCORE_MAP.getOrDefault(tier, BigDecimal.ZERO);
            total = total.add(unitScore.multiply(BigDecimal.valueOf(count)));
        }
        return total.setScale(4, RoundingMode.HALF_UP);
    }

    private void resetAndSetChallengeScore(SeasonRankingScore score, BigDecimal newScore) {
        // challengeScore를 직접 재설정: 기존 값을 빼고 새 값으로 설정
        BigDecimal delta = newScore.subtract(score.getChallengeScore());
        if (delta.compareTo(BigDecimal.ZERO) != 0) {
            score.addChallengeScore(delta);
        }
    }

    private boolean isBotCommit(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();
        return lower.startsWith("[bot]")
            || lower.startsWith("chore: bump")
            || lower.startsWith("ci:");
    }

    /**
     * 전체 유저 순위를 재계산한다 (동점자 공동 순위 = RANK() 방식).
     */
    @Transactional
    public void recalculateRanks(Season season) {
        List<SeasonRankingScore> all = rankingScoreRepository.findAllBySeason(season);

        // 총점 내림차순 정렬
        all.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));

        int rank = 1;
        for (int i = 0; i < all.size(); i++) {
            if (i > 0) {
                // 이전 유저와 점수가 같으면 동일 순위 유지 (RANK() 방식)
                boolean sameScore = all.get(i).getTotalScore()
                    .compareTo(all.get(i - 1).getTotalScore()) == 0;
                if (!sameScore) {
                    rank = i + 1;
                }
            }
            all.get(i).updateRank(rank);
            rankingScoreRepository.save(all.get(i));
        }

        log.info("[SeasonBatch] 순위 재계산 완료. seasonId={}, totalUsers={}", season.getId(), all.size());
    }

    /**
     * 백분위 기반으로 전체 유저의 티어를 배정한다. 반드시 {@code recalculateRanks} 이후 실행한다.
     *
     * <p>순위/전체인원으로 백분위를 구해 기본 5티어(BRONZE~DIAMOND)를 정하고,
     * 다이아(상위 20%)이면서 총점 기준을 넘으면 CHALLENGER({@value SeasonTier#CHALLENGER_MIN_SCORE}+)로 승급한다.</p>
     */
    @Transactional
    public void assignTiers(Season season) {
        List<SeasonRankingScore> all = rankingScoreRepository.findAllBySeason(season);
        int total = all.size();
        if (total == 0) {
            return;
        }

        int[] counts = new int[SeasonTier.values().length];
        for (SeasonRankingScore score : all) {
            Integer rank = score.getRankInSeason();
            if (rank == null) {
                continue;
            }
            double percentile = (double) rank / total;
            SeasonTier tier = SeasonTier.resolve(percentile, score.getTotalScore().doubleValue());
            score.updateTier(tier);
            rankingScoreRepository.save(score);
            counts[tier.ordinal()]++;
        }

        log.info("[SeasonBatch] 티어 배정 완료. seasonId={}, total={}, "
                + "BRONZE={}, SILVER={}, GOLD={}, PLATINUM={}, DIAMOND={}, CHALLENGER={}",
            season.getId(), total,
            counts[SeasonTier.BRONZE.ordinal()], counts[SeasonTier.SILVER.ordinal()],
            counts[SeasonTier.GOLD.ordinal()], counts[SeasonTier.PLATINUM.ordinal()],
            counts[SeasonTier.DIAMOND.ordinal()],
            counts[SeasonTier.CHALLENGER.ordinal()]);
    }
}
