package io.swkoreatech.kosp.statistics.model;

import io.swkoreatech.kosp.common.statistics.BasePlatformStatistics;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platform_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformStatistics extends BasePlatformStatistics {

    public static PlatformStatistics create(String statKey) {
        PlatformStatistics stats = new PlatformStatistics();
        stats.initializeStatKey(statKey);
        return stats;
    }
}
