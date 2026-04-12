package io.swkoreatech.kosp.common.season.repository;

import java.time.LocalDate;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonScoreEventLog;
import io.swkoreatech.kosp.common.season.model.enums.ScoreEventType;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * {@link SeasonScoreEventLog} 엔티티 리포지토리 (append-only).
 */
public interface SeasonScoreEventLogRepository extends Repository<SeasonScoreEventLog, Long> {

    SeasonScoreEventLog save(SeasonScoreEventLog log);

    /**
     * 동일 날짜 동일 이벤트 중복 지급 여부 확인 (출석, 일별 커밋 cap 등).
     */
    boolean existsBySeasonAndUserAndEventTypeAndEventDate(
        Season season, User user, ScoreEventType eventType, LocalDate eventDate
    );

    /**
     * 스트릭 보너스 중복 지급 여부 확인 (sourceRefId = 스트릭 임계값).
     */
    boolean existsBySeasonAndUserAndEventTypeAndSourceRefId(
        Season season, User user, ScoreEventType eventType, Long sourceRefId
    );

    /**
     * 특정 날짜 특정 이벤트 타입 점수 합산 (일별 cap 계산용).
     */
    long countBySeasonAndUserAndEventTypeAndEventDate(
        Season season, User user, ScoreEventType eventType, LocalDate eventDate
    );
}
