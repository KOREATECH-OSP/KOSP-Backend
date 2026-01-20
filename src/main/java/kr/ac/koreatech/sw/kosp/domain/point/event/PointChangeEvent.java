package kr.ac.koreatech.sw.kosp.domain.point.event;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public record PointChangeEvent(
    User user,
    Integer amount,
    String reason,
    PointSource source,
    User admin
) {
    public enum PointSource {
        ADMIN,
        CHALLENGE,
        ACTIVITY,
        PURCHASE,
        REFERRAL
    }

    public static PointChangeEvent fromAdmin(User user, Integer amount, String reason, User admin) {
        return new PointChangeEvent(user, amount, reason, PointSource.ADMIN, admin);
    }

    public static PointChangeEvent fromChallenge(User user, Integer amount, String reason) {
        return new PointChangeEvent(user, amount, reason, PointSource.CHALLENGE, null);
    }

    public static PointChangeEvent fromActivity(User user, Integer amount, String reason) {
        return new PointChangeEvent(user, amount, reason, PointSource.ACTIVITY, null);
    }
}
