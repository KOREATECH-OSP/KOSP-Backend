package io.swkoreatech.kosp.domain.community.team.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.domain.community.team.model.InviteRestrictionScope;

/**
 * 초대 가능 여부 응답 DTO.
 *
 * <p>프론트엔드가 초대 버튼을 누르기 전에 제한 상태를 안내할 수 있도록,
 * 차단 여부뿐 아니라 누적 거절 횟수·최근 거절 시각·제한 종료 시각을 함께 제공한다.</p>
 *
 * @param canInvite      초대 가능 여부
 * @param blockedScope   차단된 축 (TEAM/INVITER). 차단이 아니면 null
 * @param rejectionCount 누적 거절 횟수 (두 축 중 큰 값)
 * @param lastRejectedAt 최근 거절 시각 (없으면 null)
 * @param blockedUntil   제한 종료 시각 (차단이 아니면 null)
 * @param reason         사용자에게 보여줄 안내 문구 (초대 가능하면 null)
 */
public record InviteAvailabilityResponse(
    boolean canInvite,
    InviteRestrictionScope blockedScope,
    int rejectionCount,
    LocalDateTime lastRejectedAt,
    LocalDateTime blockedUntil,
    String reason
) {

    /** 초대 가능한 상태의 응답을 만든다. */
    public static InviteAvailabilityResponse available(int rejectionCount, LocalDateTime lastRejectedAt) {
        return new InviteAvailabilityResponse(true, null, rejectionCount, lastRejectedAt, null, null);
    }

    /** 초대가 차단된 상태의 응답을 만든다. */
    public static InviteAvailabilityResponse blocked(
        InviteRestrictionScope scope,
        int rejectionCount,
        LocalDateTime lastRejectedAt,
        LocalDateTime blockedUntil
    ) {
        return new InviteAvailabilityResponse(
            false, scope, rejectionCount, lastRejectedAt, blockedUntil, describe(scope, rejectionCount)
        );
    }

    private static String describe(InviteRestrictionScope scope, int rejectionCount) {
        if (scope == InviteRestrictionScope.INVITER) {
            return "이 사용자가 회원님의 초대를 %d회 거절했습니다. 24시간 후에 다시 초대할 수 있습니다."
                .formatted(rejectionCount);
        }
        return "이 사용자가 해당 팀의 초대를 %d회 거절했습니다. 24시간 후에 다시 초대할 수 있습니다."
            .formatted(rejectionCount);
    }
}
