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
import io.swkoreatech.kosp.common.season.model.enums.ScoreEventType;
import io.swkoreatech.kosp.common.season.model.enums.SeasonTier;
import io.swkoreatech.kosp.common.season.repository.SeasonProjectMemberRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonRankingScoreRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonScoreEventLogRepository;
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

    // ── 엘리트 티어(Master/Challenger) 조건 상수 (안 1: 성취 게이트형) ──
    // "상위 퍼센트 ∩ 성취 조건" 교집합. 점수만으로는 도달 불가.
    private static final double MASTER_PERCENTILE = 0.05;      // 상위 5%
    private static final double MASTER_SCORE_FLOOR = 70.0;     // 다이아 이상
    private static final int MASTER_MIN_POPULATION = 20;       // 최소 모집단
    private static final int MASTER_HIGH_TIER = 8;             // 고난도 챌린지 기준 티어
    private static final long MASTER_HIGH_TIER_COUNT = 2;
    private static final int MASTER_PROJECT_LEVEL = 4;         // 고레벨 프로젝트 기준
    private static final long MASTER_PROJECT_COUNT = 1;
    private static final int MASTER_MIN_COMMITS = 50;
    private static final long MASTER_MIN_ATTENDANCE = 20;

    private static final double CHALLENGER_PERCENTILE = 0.01;  // 상위 1%
    private static final double CHALLENGER_SCORE_FLOOR = 80.0;
    private static final int CHALLENGER_MIN_POPULATION = 50;
    private static final long CHALLENGER_HIGH_TIER_COUNT = 3;  // 티어 8+ 3개 이상
    private static final long CHALLENGER_PROJECT_COUNT = 1;    // 레벨 4+ 완료 1개 이상
    private static final int CHALLENGER_MIN_COMMITS = 100;
    private static final long CHALLENGER_MIN_ATTENDANCE = 40;
    private static final int CHALLENGER_MIN_DIVERSITY = 4;     // 5개 중 4개 카테고리+

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
    private final SeasonProjectMemberRepository seasonProjectMemberRepository;
    private final SeasonScoreEventLogRepository eventLogRepository;

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
            applyEliteTiers(season);

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
     * 엘리트 티어(Master/Challenger)를 승격 적용한다.
     *
     * <p>점수형 티어(Bronze~Diamond) 위에, "상위 퍼센트 ∩ 성취 조건"(교집합)을 만족하는
     * 유저를 Master 또는 Challenger로 승격한다. 반드시 {@code recalculateRanks} 이후 실행한다.</p>
     *
     * <p>성능: 점수 하한·상위% 후보만 성취 조건 쿼리를 수행하므로 대다수 유저는 스킵된다.</p>
     */
    @Transactional
    public void applyEliteTiers(Season season) {
        List<SeasonRankingScore> all = rankingScoreRepository.findAllBySeason(season);
        int total = all.size();
        if (total == 0) {
            return;
        }

        int masterCount = 0;
        int challengerCount = 0;

        for (SeasonRankingScore score : all) {
            Integer rank = score.getRankInSeason();
            if (rank == null) {
                continue;
            }
            double percentile = (double) rank / total;
            double totalScore = score.getTotalScore().doubleValue();

            // 후보 필터: Master(느슨한 게이트) 기준도 못 넘으면 성취 조건 쿼리 없이 스킵
            if (total < MASTER_MIN_POPULATION
                || percentile > MASTER_PERCENTILE
                || totalScore < MASTER_SCORE_FLOOR) {
                continue;
            }

            EliteStats stats = collectEliteStats(season, score);

            if (meetsChallenger(percentile, totalScore, total, stats)) {
                score.updateTier(SeasonTier.CHALLENGER);
                rankingScoreRepository.save(score);
                challengerCount++;
            } else if (meetsMaster(stats)) {
                score.updateTier(SeasonTier.MASTER_1);
                rankingScoreRepository.save(score);
                masterCount++;
            }
        }

        log.info("[SeasonBatch] 엘리트 티어 적용 완료. seasonId={}, master={}, challenger={}",
            season.getId(), masterCount, challengerCount);
    }

    private EliteStats collectEliteStats(Season season, SeasonRankingScore score) {
        User user = score.getUser();
        var startDateTime = season.getStartDate().atStartOfDay();
        var endDateTime = season.getEndDate().plusDays(1).atStartOfDay();

        List<Object[]> tierCounts = challengeHistoryRepository
            .countAchievedGroupByTierInPeriod(user, startDateTime, endDateTime);
        long highTierChallenges = countChallengesFromTier(tierCounts, MASTER_HIGH_TIER);

        long highLevelProjects = seasonProjectMemberRepository
            .countCompletedProjectsByLevel(user, season, MASTER_PROJECT_LEVEL);

        int validCommits = score.getCommitScore()
            .divide(COMMIT_SCORE_PER_UNIT, 0, RoundingMode.HALF_UP)
            .intValue();

        long attendanceDays = eventLogRepository
            .countBySeasonAndUserAndEventType(season, user, ScoreEventType.ATTENDANCE);

        int diversity = score.activeCategoryCount();

        return new EliteStats(highTierChallenges, highLevelProjects, validCommits, attendanceDays, diversity);
    }

    private long countChallengesFromTier(List<Object[]> tierCounts, int minTier) {
        long sum = 0;
        for (Object[] row : tierCounts) {
            Integer tier = (Integer) row[0];
            Long count = (Long) row[1];
            if (tier != null && count != null && tier >= minTier) {
                sum += count;
            }
        }
        return sum;
    }

    /** Master 승급 조건: (고난도 챌린지 OR 고레벨 프로젝트) ∩ 커밋 하한 ∩ 출석 하한. (후보 필터에서 상위%·점수 이미 통과) */
    private boolean meetsMaster(EliteStats s) {
        boolean achievement = s.highTierChallenges() >= MASTER_HIGH_TIER_COUNT
            || s.highLevelProjects() >= MASTER_PROJECT_COUNT;
        return achievement
            && s.validCommits() >= MASTER_MIN_COMMITS
            && s.attendanceDays() >= MASTER_MIN_ATTENDANCE;
    }

    /** Challenger 승급 조건: 상위 1% ∩ 점수 하한 ∩ 고난도 챌린지 ∩ 고레벨 프로젝트 ∩ 커밋/출석/다양성 하한. */
    private boolean meetsChallenger(double percentile, double totalScore, int total, EliteStats s) {
        return percentile <= CHALLENGER_PERCENTILE
            && totalScore >= CHALLENGER_SCORE_FLOOR
            && total >= CHALLENGER_MIN_POPULATION
            && s.highTierChallenges() >= CHALLENGER_HIGH_TIER_COUNT
            && s.highLevelProjects() >= CHALLENGER_PROJECT_COUNT
            && s.validCommits() >= CHALLENGER_MIN_COMMITS
            && s.attendanceDays() >= CHALLENGER_MIN_ATTENDANCE
            && s.diversity() >= CHALLENGER_MIN_DIVERSITY;
    }

    /** 엘리트 티어 조건 평가용 집계값. */
    private record EliteStats(
        long highTierChallenges,
        long highLevelProjects,
        int validCommits,
        long attendanceDays,
        int diversity
    ) {
    }
}
