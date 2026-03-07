package io.swkoreatech.kosp.statistics.model;

import io.swkoreatech.kosp.common.model.BasePlatformStatistics;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 플랫폼 전체 통계를 저장하는 JPA 엔티티.
 *
 * <p>{@link BasePlatformStatistics}를 상속하여 플랫폼 전체 사용자의
 * 평균 커밋 수, 스타 수, PR 수, 이슈 수 등의 통계를 관리한다.
 */
@Entity
@Table(name = "platform_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformStatistics extends BasePlatformStatistics {

    /**
     * 지정된 통계 키로 새로운 PlatformStatistics를 생성한다.
     *
     * @param statKey 통계 식별 키 (예: "global")
     * @return 초기화된 PlatformStatistics 인스턴스
     */
    public static PlatformStatistics create(String statKey) {
        PlatformStatistics stats = new PlatformStatistics();
        stats.initializeStatKey(statKey);
        return stats;
    }
}
