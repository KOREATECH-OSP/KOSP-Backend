package io.swkoreatech.kosp.domain.github.model;

import io.swkoreatech.kosp.common.model.BasePlatformStatistics;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 플랫폼 통계 엔티티.
 * 전체 사용자의 평균 기여 통계 정보를 관리한다.
 */
@Entity
@Table(name = "platform_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformStatistics extends BasePlatformStatistics {

}
