package io.swkoreatech.kosp.domain.community.team.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 초대 반복 거절 제한 규칙 단위 테스트.
 *
 * <p>정책: 영구 3회 누적 + rolling 24시간 cooldown.</p>
 */
class InviteRestrictionTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 8, 2, 12, 0);

    private InviteRestriction newRestriction() {
        return InviteRestriction.builder()
            .scope(InviteRestrictionScope.TEAM)
            .scopeId(1L)
            .inviteeId(2L)
            .build();
    }

    @Nested
    @DisplayName("누적 거절 임계값")
    class Threshold {

        @Test
        @DisplayName("거절 1~2회는 재초대를 차단하지 않는다")
        void notBlockedBeforeThreshold() {
            InviteRestriction restriction = newRestriction();

            restriction.recordRejection(BASE);
            assertThat(restriction.isBlockedAt(BASE.plusMinutes(1))).isFalse();

            restriction.recordRejection(BASE.plusMinutes(5));
            assertThat(restriction.isBlockedAt(BASE.plusMinutes(6))).isFalse();
            assertThat(restriction.getRejectionCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("거절 3회에 도달하면 24시간 동안 차단된다")
        void blockedAtThreshold() {
            InviteRestriction restriction = newRestriction();

            restriction.recordRejection(BASE);
            restriction.recordRejection(BASE);
            restriction.recordRejection(BASE);

            assertThat(restriction.getRejectionCount()).isEqualTo(3);
            assertThat(restriction.getBlockedUntil()).isEqualTo(BASE.plusHours(24));
            assertThat(restriction.isBlockedAt(BASE.plusHours(23))).isTrue();
        }
    }

    @Nested
    @DisplayName("cooldown 경계")
    class Cooldown {

        private InviteRestriction blocked() {
            InviteRestriction restriction = newRestriction();
            restriction.recordRejection(BASE);
            restriction.recordRejection(BASE);
            restriction.recordRejection(BASE);
            return restriction;
        }

        @Test
        @DisplayName("제한 시간 이전에는 재초대할 수 없다")
        void blockedBeforeCooldownEnds() {
            assertThat(blocked().isBlockedAt(BASE.plusHours(23).plusMinutes(59))).isTrue();
        }

        @Test
        @DisplayName("제한 해제 이후에는 재초대할 수 있다")
        void allowedAfterCooldown() {
            assertThat(blocked().isBlockedAt(BASE.plusHours(24))).isFalse();
            assertThat(blocked().isBlockedAt(BASE.plusHours(25))).isFalse();
        }

        @Test
        @DisplayName("cooldown 이 끝나도 누적 횟수는 초기화되지 않아 다음 거절에 즉시 재차단된다")
        void countIsNotResetAfterCooldown() {
            InviteRestriction restriction = blocked();
            LocalDateTime afterCooldown = BASE.plusHours(25);
            assertThat(restriction.isBlockedAt(afterCooldown)).isFalse();

            // 재초대 후 다시 거절당한 상황
            restriction.recordRejection(afterCooldown);

            assertThat(restriction.getRejectionCount()).isEqualTo(4);
            assertThat(restriction.isBlockedAt(afterCooldown.plusHours(1))).isTrue();
            assertThat(restriction.getBlockedUntil()).isEqualTo(afterCooldown.plusHours(24));
        }
    }

    @Test
    @DisplayName("거절 이력이 없으면 차단되지 않는다")
    void noRejectionMeansNoBlock() {
        assertThat(newRestriction().isBlockedAt(BASE)).isFalse();
    }
}
