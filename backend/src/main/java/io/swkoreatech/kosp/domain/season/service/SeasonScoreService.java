package io.swkoreatech.kosp.domain.season.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonRankingScore;
import io.swkoreatech.kosp.common.season.model.SeasonScoreEventLog;
import io.swkoreatech.kosp.common.season.model.enums.ScoreEventType;
import io.swkoreatech.kosp.common.season.repository.SeasonRankingScoreRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonScoreEventLogRepository;
import io.swkoreatech.kosp.common.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시즌 점수 지급 서비스.
 *
 * <p>점수 이벤트를 감사 원장({@link SeasonScoreEventLog})에 기록하고
 * {@link SeasonRankingScore}의 카테고리별 점수를 갱신한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonScoreService {

    private final SeasonRankingScoreRepository rankingScoreRepository;
    private final SeasonScoreEventLogRepository eventLogRepository;

    /**
     * 점수를 지급하고 감사 원장에 기록한다.
     *
     * @param season        대상 시즌
     * @param user          대상 유저
     * @param eventType     점수 이벤트 타입
     * @param delta         지급할 점수
     * @param eventDate     점수 귀속 날짜
     * @param sourceRefId   연관 엔티티 ID (없으면 null)
     * @param sourceRefType 연관 엔티티 타입 (없으면 null)
     */
    @Transactional
    public void addScore(
        Season season,
        User user,
        ScoreEventType eventType,
        BigDecimal delta,
        LocalDate eventDate,
        Long sourceRefId,
        String sourceRefType
    ) {
        if (delta.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // 감사 원장 기록
        SeasonScoreEventLog eventLog = SeasonScoreEventLog.builder()
            .season(season)
            .user(user)
            .eventType(eventType)
            .scoreDelta(delta)
            .sourceRefId(sourceRefId)
            .sourceRefType(sourceRefType)
            .eventDate(eventDate)
            .build();
        eventLogRepository.save(eventLog);

        // 집계 테이블 갱신
        SeasonRankingScore rankingScore = getOrCreate(season, user);
        applyScore(rankingScore, eventType, delta);
        rankingScore.recalculate();
        rankingScoreRepository.save(rankingScore);

        log.debug("[SeasonScore] userId={}, type={}, delta={}, date={}", user.getId(), eventType, delta, eventDate);
    }

    /**
     * 시즌 점수 집계 레코드를 조회하거나 없으면 새로 생성한다.
     */
    @Transactional
    public SeasonRankingScore getOrCreate(Season season, User user) {
        return rankingScoreRepository.findBySeasonAndUser(season, user)
            .orElseGet(() -> rankingScoreRepository.save(
                SeasonRankingScore.builder()
                    .season(season)
                    .user(user)
                    .build()
            ));
    }

    private void applyScore(SeasonRankingScore score, ScoreEventType eventType, BigDecimal delta) {
        switch (eventType) {
            case ATTENDANCE, STREAK_BONUS -> score.addAttendanceScore(delta);
            case CHALLENGE -> score.addChallengeScore(delta);
            case PROJECT -> score.addProjectScore(delta);
            case COMMUNITY -> score.addCommunityScore(delta);
            case COMMIT -> score.updateCommitScore(score.getCommitScore().add(delta));
        }
    }
}
