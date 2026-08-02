package io.swkoreatech.kosp.domain.community.team.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.community.team.dto.response.InviteAvailabilityResponse;
import io.swkoreatech.kosp.domain.community.team.model.InviteRestriction;
import io.swkoreatech.kosp.domain.community.team.model.InviteRestrictionScope;
import io.swkoreatech.kosp.domain.community.team.repository.InviteRestrictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 팀 초대 반복 거절 제한 서비스.
 *
 * <p>제한은 두 축으로 동시에 집계된다.</p>
 * <ul>
 *   <li>{@link InviteRestrictionScope#TEAM} — 같은 팀의 다른 관리자가 우회 초대하는 것을 막는다.</li>
 *   <li>{@link InviteRestrictionScope#INVITER} — 같은 초대자가 다른 팀으로 우회 초대하는 것을 막는다.</li>
 * </ul>
 *
 * <p>초대 시에는 두 축 중 <b>하나라도</b> 차단이면 거부하고,
 * 거절 시에는 두 축 <b>모두</b> 누적 횟수를 올린다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InviteRestrictionService {

    private final InviteRestrictionRepository restrictionRepository;

    /**
     * 초대 가능 여부를 조회한다.
     *
     * @param teamId    초대하려는 팀 ID
     * @param inviterId 초대자 사용자 ID
     * @param inviteeId 피초대자 사용자 ID
     * @return 초대 가능 여부와 제한 상세 (거절 횟수, 최근 거절 시각, 제한 종료 시각)
     */
    public InviteAvailabilityResponse getAvailability(Long teamId, Long inviterId, Long inviteeId) {
        LocalDateTime now = LocalDateTime.now();

        Optional<InviteRestriction> teamScope = find(InviteRestrictionScope.TEAM, teamId, inviteeId);
        Optional<InviteRestriction> inviterScope = find(InviteRestrictionScope.INVITER, inviterId, inviteeId);

        // 두 축 중 실제로 차단 중인 것을 고른다. 둘 다 차단이면 더 늦게 풀리는 쪽을 기준으로 안내한다.
        Optional<InviteRestriction> blocking = List.of(teamScope, inviterScope).stream()
            .flatMap(Optional::stream)
            .filter(r -> r.isBlockedAt(now))
            .max((left, right) -> left.getBlockedUntil().compareTo(right.getBlockedUntil()));

        if (blocking.isEmpty()) {
            int maxCount = Math.max(
                teamScope.map(InviteRestriction::getRejectionCount).orElse(0),
                inviterScope.map(InviteRestriction::getRejectionCount).orElse(0)
            );
            LocalDateTime lastRejectedAt = latestRejection(teamScope, inviterScope);
            return InviteAvailabilityResponse.available(maxCount, lastRejectedAt);
        }

        InviteRestriction restriction = blocking.get();
        return InviteAvailabilityResponse.blocked(
            restriction.getScope(),
            restriction.getRejectionCount(),
            restriction.getLastRejectedAt(),
            restriction.getBlockedUntil()
        );
    }

    /**
     * 초대가 제한되어 있는지 여부만 빠르게 판정한다.
     *
     * @return 차단 중이면 {@code true}
     */
    public boolean isBlocked(Long teamId, Long inviterId, Long inviteeId) {
        LocalDateTime now = LocalDateTime.now();
        return find(InviteRestrictionScope.TEAM, teamId, inviteeId).map(r -> r.isBlockedAt(now)).orElse(false)
            || find(InviteRestrictionScope.INVITER, inviterId, inviteeId).map(r -> r.isBlockedAt(now)).orElse(false);
    }

    /**
     * 거절 1건을 두 축(TEAM, INVITER) 모두에 기록한다.
     *
     * @param teamId    거절된 초대의 팀 ID
     * @param inviterId 거절된 초대를 보낸 사용자 ID
     * @param inviteeId 거절한 사용자 ID
     */
    @Transactional
    public void recordRejection(Long teamId, Long inviterId, Long inviteeId) {
        LocalDateTime now = LocalDateTime.now();
        recordOn(InviteRestrictionScope.TEAM, teamId, inviteeId, now);
        recordOn(InviteRestrictionScope.INVITER, inviterId, inviteeId, now);
    }

    private void recordOn(InviteRestrictionScope scope, Long scopeId, Long inviteeId, LocalDateTime now) {
        InviteRestriction restriction = find(scope, scopeId, inviteeId)
            .orElseGet(() -> InviteRestriction.builder()
                .scope(scope)
                .scopeId(scopeId)
                .inviteeId(inviteeId)
                .build());

        restriction.recordRejection(now);
        restrictionRepository.save(restriction);

        log.info("초대 거절 기록: scope={}, scopeId={}, inviteeId={}, count={}, blockedUntil={}",
            scope, scopeId, inviteeId, restriction.getRejectionCount(), restriction.getBlockedUntil());
    }

    private Optional<InviteRestriction> find(InviteRestrictionScope scope, Long scopeId, Long inviteeId) {
        if (scopeId == null || inviteeId == null) {
            return Optional.empty();
        }
        return restrictionRepository.findByScopeAndScopeIdAndInviteeId(scope, scopeId, inviteeId);
    }

    private LocalDateTime latestRejection(Optional<InviteRestriction> left, Optional<InviteRestriction> right) {
        return List.of(left, right).stream()
            .flatMap(Optional::stream)
            .map(InviteRestriction::getLastRejectedAt)
            .filter(java.util.Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);
    }
}
