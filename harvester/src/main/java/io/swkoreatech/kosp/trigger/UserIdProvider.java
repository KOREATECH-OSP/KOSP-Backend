package io.swkoreatech.kosp.trigger;

import java.util.List;

public interface UserIdProvider {
    List<Long> findActiveUserIds();
}
