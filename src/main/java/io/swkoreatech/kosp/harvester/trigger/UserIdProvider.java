package io.swkoreatech.kosp.harvester.trigger;

import java.util.List;

public interface UserIdProvider {
    List<Long> findActiveUserIds();
}
