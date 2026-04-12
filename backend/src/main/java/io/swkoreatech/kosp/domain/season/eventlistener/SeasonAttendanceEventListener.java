package io.swkoreatech.kosp.domain.season.eventlistener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.enums.ScoreEventType;
import io.swkoreatech.kosp.common.season.repository.SeasonRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonScoreEventLogRepository;
import io.swkoreatech.kosp.common.title.model.UserLoginStreak;
import io.swkoreatech.kosp.common.title.repository.UserLoginStreakRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.season.service.SeasonScoreService;
import io.swkoreatech.kosp.domain.user.event.UserLoginEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 이벤트 → 시즌 출석 점수 지급 이벤트 리스너.
 *
 * <p>@Async로 처리되므로 로그인 응답에 영향을 주지 않는다.
 * 예외가 발생해도 흡수하여 로그인 플로우를 방해하지 않는다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeasonAttendanceEventListener {

    private static final BigDecimal ATTENDANCE_DAILY_SCORE = new BigDecimal("0.0100");

    // 스트릭 보너스: threshold(일) → 점수
    private static final int[] STREAK_THRESHOLDS = {7, 30, 50, 100};
    private static final BigDecimal[] STREAK_BONUSES = {
        new BigDecimal("1.0000"),
        new BigDecimal("3.0000"),
        new BigDecimal("5.0000"),
        new BigDecimal("10.0000")
    };

    private final SeasonRepository seasonRepository;
    private final SeasonScoreEventLogRepository eventLogRepository;
    private final UserRepository userRepository;
    private final UserLoginStreakRepository loginStreakRepository;
    private final SeasonScoreService seasonScoreService;

    @Async
    @EventListener
    public void handle(UserLoginEvent event) {
        try {
            processAttendance(event.getUserId());
        } catch (Exception e) {
            log.error("[SeasonAttendance] 출석 점수 처리 중 오류. userId={}", event.getUserId(), e);
        }
    }

    private void processAttendance(Long userId) {
        Optional<Season> activeSeason = seasonRepository.findByIsActiveTrue();
        if (activeSeason.isEmpty()) {
            return;
        }

        Season season = activeSeason.get();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        LocalDate today = LocalDate.now();

        // 오늘 이미 출석 점수 지급했는지 확인
        boolean alreadyGranted = eventLogRepository.existsBySeasonAndUserAndEventTypeAndEventDate(
            season, user, ScoreEventType.ATTENDANCE, today);
        if (alreadyGranted) {
            log.debug("[SeasonAttendance] 오늘 이미 출석 처리됨. userId={}", userId);
            return;
        }

        // 출석 기본 점수 지급
        seasonScoreService.addScore(season, user, ScoreEventType.ATTENDANCE,
            ATTENDANCE_DAILY_SCORE, today, null, null);

        // 스트릭 보너스 체크
        loginStreakRepository.findByUser(user).ifPresent(streak ->
            checkAndGrantStreakBonus(season, user, streak, today)
        );
    }

    private void checkAndGrantStreakBonus(Season season, User user, UserLoginStreak streak, LocalDate today) {
        int currentStreak = streak.getCurrentStreak();

        for (int i = 0; i < STREAK_THRESHOLDS.length; i++) {
            if (currentStreak == STREAK_THRESHOLDS[i]) {
                long thresholdAsRefId = STREAK_THRESHOLDS[i];

                boolean alreadyGranted = eventLogRepository.existsBySeasonAndUserAndEventTypeAndSourceRefId(
                    season, user, ScoreEventType.STREAK_BONUS, thresholdAsRefId);

                if (!alreadyGranted) {
                    seasonScoreService.addScore(season, user, ScoreEventType.STREAK_BONUS,
                        STREAK_BONUSES[i], today, thresholdAsRefId, "STREAK_THRESHOLD");
                    log.info("[SeasonAttendance] 스트릭 보너스 지급. userId={}, threshold={}일, bonus={}",
                        user.getId(), STREAK_THRESHOLDS[i], STREAK_BONUSES[i]);
                }
            }
        }
    }
}
