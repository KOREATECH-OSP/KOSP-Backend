package io.swkoreatech.kosp.common.terms.model;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 사용자 약관 동의 이력 엔티티.
 *
 * <p>특정 사용자가 특정 버전의 약관에 동의한 이력을 기록한다.
 * (user_id, terms_id) 조합은 UNIQUE 제약으로 중복 저장을 방지한다.</p>
 */
@Getter
@Entity
@Table(name = "user_terms_agreement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTermsAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    @Builder
    private UserTermsAgreement(User user, Terms terms, LocalDateTime agreedAt) {
        this.user = user;
        this.terms = terms;
        this.agreedAt = agreedAt;
    }
}
