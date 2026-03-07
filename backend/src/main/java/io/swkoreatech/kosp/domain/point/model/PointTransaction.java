package io.swkoreatech.kosp.domain.point.model;

import static lombok.AccessLevel.PROTECTED;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.user.model.User;
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
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포인트 거래 엔티티.
 * 사용자의 포인트 변동 내역을 기록한다.
 */
@Getter
@Entity
@Table(name = "point_transactions")
@NoArgsConstructor(access = PROTECTED)
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "amount", nullable = false)
    private Integer amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private PointSource source;

    @NotNull
    @Column(name = "reason", nullable = false)
    private String reason;

    @NotNull
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Builder
    private PointTransaction(
        User user,
        Integer amount,
        TransactionType type,
        PointSource source,
        String reason,
        Integer balanceAfter
    ) {
        this.user = user;
        this.amount = amount;
        this.type = type;
        this.source = source;
        this.reason = reason;
        this.balanceAfter = balanceAfter;
    }
}
