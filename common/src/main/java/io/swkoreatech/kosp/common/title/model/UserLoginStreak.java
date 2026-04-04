package io.swkoreatech.kosp.common.title.model;

import java.time.LocalDate;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 유저 로그인 연속 접속 스트릭 엔티티.
 *
 * <p>유저 로그인 시 {@code UserLoginStreakService}에서 업데이트된다.
 * 전날 대비 당일 로그인이면 streak 증가, 같은 날이면 유지, 이틀 이상 공백이면 1로 초기화.</p>
 *
 * <p>TODO: 현재 로그인 이벤트({@code UserLoginEvent}) 기반으로 동작.
 * 로그인 없이도 활동(커밋 등)이 있을 경우 별도 streak 집계 전략 고려 필요.</p>
 */
@Getter
@Entity
@Table(name = "user_login_streak")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginStreak extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    @Builder
    private UserLoginStreak(User user) {
        this.user = user;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.lastLoginDate = null;
    }

    /**
     * 로그인 시각을 기준으로 streak를 갱신한다.
     *
     * @param today 오늘 날짜
     */
    public void recordLogin(LocalDate today) {
        if (lastLoginDate == null) {
            currentStreak = 1;
        } else if (lastLoginDate.equals(today)) {
            // 당일 재로그인 - 변경 없음
            return;
        } else if (lastLoginDate.equals(today.minusDays(1))) {
            // 전날 로그인 → 연속 유지
            currentStreak++;
        } else {
            // 이틀 이상 공백 → 초기화
            currentStreak = 1;
        }
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }
        lastLoginDate = today;
    }
}
