package io.swkoreatech.kosp.common.title.model;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.title.model.enums.TitleConditionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 칭호 조건 엔티티.
 *
 * <p>하나의 칭호({@link Title})에 여러 조건이 있을 수 있으며,
 * 모든 조건을 충족해야 칭호가 지급된다(AND 로직).
 * 새 조건 유형 추가 시 {@link TitleConditionType}에 값을 추가하고
 * 대응하는 {@code TitleConditionEvaluator} 구현체를 Spring Bean으로 등록한다.</p>
 */
@Getter
@Entity
@Table(name = "title_condition")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TitleCondition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 50)
    private TitleConditionType conditionType;

    @Column(name = "threshold_value", nullable = false)
    private int thresholdValue;

    @Column(length = 255)
    private String description;

    @Builder
    private TitleCondition(
        Title title,
        TitleConditionType conditionType,
        int thresholdValue,
        String description
    ) {
        this.title = title;
        this.conditionType = conditionType;
        this.thresholdValue = thresholdValue;
        this.description = description;
    }
}
