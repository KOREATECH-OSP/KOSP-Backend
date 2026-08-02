package io.swkoreatech.kosp.domain.community.team.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.swkoreatech.kosp.domain.community.team.model.InviteRestriction;
import io.swkoreatech.kosp.domain.community.team.model.InviteRestrictionScope;
import io.swkoreatech.kosp.domain.community.team.repository.InviteRestrictionRepository;

/**
 * 초대 제한 서비스의 우회 차단 시나리오 테스트.
 *
 * <p>요구된 3가지 핵심 시나리오를 검증한다.</p>
 * <ol>
 *   <li>같은 팀의 다른 관리자가 우회 초대하면 안 됨</li>
 *   <li>같은 초대자가 다른 팀을 이용해 우회 초대하면 안 됨</li>
 *   <li>제한 시간 이전 재초대 차단 / 해제 이후 허용</li>
 * </ol>
 */
class InviteRestrictionServiceTest {

    private static final Long TEAM_A = 100L;
    private static final Long TEAM_B = 200L;
    private static final Long ADMIN_CHULSOO = 10L;   // 철수 (여러 팀 관리자)
    private static final Long ADMIN_MINSU = 11L;     // 민수 (팀 A의 또 다른 관리자)
    private static final Long INVITEE_YOUNGHEE = 20L; // 영희

    private InMemoryRestrictionRepository repository;
    private InviteRestrictionService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRestrictionRepository();
        service = new InviteRestrictionService(repository);
    }

    /** 특정 초대자가 특정 팀에서 영희에게 n회 거절당한 상황을 만든다. */
    private void rejectTimes(Long teamId, Long inviterId, int times) {
        for (int i = 0; i < times; i++) {
            service.recordRejection(teamId, inviterId, INVITEE_YOUNGHEE);
        }
    }

    @Test
    @DisplayName("같은 팀/같은 초대자로 3회 거절되면 재초대가 차단된다")
    void blockedAfterThreeRejectionsSameTeamSameInviter() {
        rejectTimes(TEAM_A, ADMIN_CHULSOO, 3);

        assertThat(service.isBlocked(TEAM_A, ADMIN_CHULSOO, INVITEE_YOUNGHEE)).isTrue();
    }

    @Test
    @DisplayName("정책 A — 같은 팀의 다른 관리자가 우회 초대할 수 없다")
    void blockedForAnotherAdminOfSameTeam() {
        // 철수가 A팀에서 영희에게 3회 거절당함
        rejectTimes(TEAM_A, ADMIN_CHULSOO, 3);

        // 같은 A팀의 다른 관리자 민수가 초대 시도 → TEAM 스코프로 차단되어야 한다
        assertThat(service.isBlocked(TEAM_A, ADMIN_MINSU, INVITEE_YOUNGHEE)).isTrue();
    }

    @Test
    @DisplayName("정책 B — 같은 초대자가 자신의 다른 팀으로 우회 초대할 수 없다")
    void blockedForSameInviterOnAnotherTeam() {
        // 철수가 A팀에서 영희에게 3회 거절당함
        rejectTimes(TEAM_A, ADMIN_CHULSOO, 3);

        // 철수가 자신이 관리하는 B팀으로 초대 시도 → INVITER 스코프로 차단되어야 한다
        assertThat(service.isBlocked(TEAM_B, ADMIN_CHULSOO, INVITEE_YOUNGHEE)).isTrue();
    }

    @Test
    @DisplayName("무관한 팀의 무관한 관리자는 초대할 수 있다")
    void unrelatedTeamAndInviterIsAllowed() {
        rejectTimes(TEAM_A, ADMIN_CHULSOO, 3);

        // B팀의 민수 — TEAM(B)도 INVITER(민수)도 거절 이력이 없다
        assertThat(service.isBlocked(TEAM_B, ADMIN_MINSU, INVITEE_YOUNGHEE)).isFalse();
    }

    @Test
    @DisplayName("거절 2회까지는 어느 경로로도 차단되지 않는다")
    void notBlockedBelowThreshold() {
        rejectTimes(TEAM_A, ADMIN_CHULSOO, 2);

        assertThat(service.isBlocked(TEAM_A, ADMIN_CHULSOO, INVITEE_YOUNGHEE)).isFalse();
        assertThat(service.isBlocked(TEAM_A, ADMIN_MINSU, INVITEE_YOUNGHEE)).isFalse();
        assertThat(service.isBlocked(TEAM_B, ADMIN_CHULSOO, INVITEE_YOUNGHEE)).isFalse();
    }

    @Test
    @DisplayName("서로 다른 팀에서 1회씩 거절해도 초대자 축에는 누적된다")
    void inviterScopeAccumulatesAcrossTeams() {
        // 철수가 팀을 바꿔가며 3회 초대 → 3회 모두 거절
        service.recordRejection(TEAM_A, ADMIN_CHULSOO, INVITEE_YOUNGHEE);
        service.recordRejection(TEAM_B, ADMIN_CHULSOO, INVITEE_YOUNGHEE);
        service.recordRejection(300L, ADMIN_CHULSOO, INVITEE_YOUNGHEE);

        // 팀별로는 각 1회라 TEAM 스코프는 미달이지만, INVITER 스코프가 3회이므로 차단된다
        assertThat(service.isBlocked(400L, ADMIN_CHULSOO, INVITEE_YOUNGHEE)).isTrue();
    }

    @Test
    @DisplayName("초대 가능 여부 응답에 거절 횟수·최근 거절 시각·제한 종료 시각이 담긴다")
    void availabilityExposesRestrictionDetail() {
        rejectTimes(TEAM_A, ADMIN_CHULSOO, 3);

        var response = service.getAvailability(TEAM_A, ADMIN_CHULSOO, INVITEE_YOUNGHEE);

        assertThat(response.canInvite()).isFalse();
        assertThat(response.rejectionCount()).isEqualTo(3);
        assertThat(response.lastRejectedAt()).isNotNull();
        assertThat(response.blockedUntil()).isNotNull();
        assertThat(response.reason()).contains("24시간");
    }

    @Test
    @DisplayName("제한이 없으면 초대 가능으로 응답한다")
    void availabilityWhenNoRestriction() {
        var response = service.getAvailability(TEAM_A, ADMIN_CHULSOO, INVITEE_YOUNGHEE);

        assertThat(response.canInvite()).isTrue();
        assertThat(response.rejectionCount()).isZero();
        assertThat(response.blockedUntil()).isNull();
        assertThat(response.reason()).isNull();
    }

    /**
     * 테스트용 인메모리 리포지토리.
     * (scope, scopeId, inviteeId) 조합으로 저장·조회한다.
     */
    private static class InMemoryRestrictionRepository implements InviteRestrictionRepository {

        private final List<InviteRestriction> store = new ArrayList<>();

        @Override
        public InviteRestriction save(InviteRestriction restriction) {
            if (!store.contains(restriction)) {
                store.add(restriction);
            }
            return restriction;
        }

        @Override
        public Optional<InviteRestriction> findByScopeAndScopeIdAndInviteeId(
            InviteRestrictionScope scope, Long scopeId, Long inviteeId) {
            return store.stream()
                .filter(r -> r.getScope() == scope
                    && r.getScopeId().equals(scopeId)
                    && r.getInviteeId().equals(inviteeId))
                .findFirst();
        }

        @Override
        public List<InviteRestriction> findAllByInviteeId(Long inviteeId) {
            return store.stream().filter(r -> r.getInviteeId().equals(inviteeId)).toList();
        }
    }
}
