package io.swkoreatech.kosp.common.season.model;

import java.time.LocalDate;

import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시즌 엔티티.
 *
 * <p>랭킹 시즌의 이름, 기간, 활성 여부를 관리한다.</p>
 */
@Getter
@Entity
@Table(name = "season")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Season extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Builder
    private Season(String name, LocalDate startDate, LocalDate endDate, boolean isActive) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
