package io.swkoreatech.kosp.domain.point.event;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.point.model.PointSource;

/**
 * 포인트 변경 이벤트.
 * 포인트 변경 시 발행되는 애플리케이션 이벤트이다.
 *
 * @param user 대상 사용자
 * @param amount 변경 포인트 (양수: 지급, 음수: 차감)
 * @param reason 변경 사유
 * @param source 포인트 출처
 */
public record PointChangeEvent(
    User user,
    Integer amount,
    String reason,
    PointSource source
) {
    /**
     * 관리자 포인트 변경 이벤트를 생성한다.
     *
     * @param user 대상 사용자
     * @param amount 변경 포인트
     * @param reason 변경 사유
     * @return 포인트 변경 이벤트
     */
    public static PointChangeEvent fromAdmin(User user, Integer amount, String reason) {
        return new PointChangeEvent(user, amount, reason, PointSource.ADMIN);
    }

    /**
     * 챌린지 달성 포인트 변경 이벤트를 생성한다.
     *
     * @param user 대상 사용자
     * @param amount 변경 포인트
     * @param reason 변경 사유
     * @return 포인트 변경 이벤트
     */
    public static PointChangeEvent fromChallenge(User user, Integer amount, String reason) {
        return new PointChangeEvent(user, amount, reason, PointSource.CHALLENGE);
    }

    /**
     * 활동 보상 포인트 변경 이벤트를 생성한다.
     *
     * @param user 대상 사용자
     * @param amount 변경 포인트
     * @param reason 변경 사유
     * @return 포인트 변경 이벤트
     */
    public static PointChangeEvent fromActivity(User user, Integer amount, String reason) {
        return new PointChangeEvent(user, amount, reason, PointSource.ACTIVITY);
    }
}
