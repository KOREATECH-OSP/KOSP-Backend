package io.swkoreatech.kosp.domain.point.event;

import io.swkoreatech.kosp.domain.point.model.PointSource;
import io.swkoreatech.kosp.domain.user.model.User;

public record PointChangeEvent(
    User user,
    Integer amount,
    String reason,
    PointSource source
) {
    public static PointChangeEvent fromAdmin(User user, Integer amount, String reason) {
        return new PointChangeEvent(user, amount, reason, PointSource.ADMIN);
    }

    public static PointChangeEvent fromChallenge(User user, Integer amount, String reason) {
        return new PointChangeEvent(user, amount, reason, PointSource.CHALLENGE);
    }

    public static PointChangeEvent fromActivity(User user, Integer amount, String reason) {
        return new PointChangeEvent(user, amount, reason, PointSource.ACTIVITY);
    }
}
