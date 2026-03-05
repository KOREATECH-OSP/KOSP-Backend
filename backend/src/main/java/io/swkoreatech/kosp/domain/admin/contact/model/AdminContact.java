package io.swkoreatech.kosp.domain.admin.contact.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 연락처 엔티티.
 * <p>싱글턴 패턴으로 구현되어 하나의 레코드만 유지한다.</p>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "admin_contact")
public class AdminContact {

    private static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(nullable = false)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private AdminContact(String email) {
        this.id = SINGLETON_ID;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 기본 관리자 연락처를 생성한다.
     *
     * @return 기본 이메일이 설정된 관리자 연락처 엔티티
     */
    public static AdminContact createDefault() {
        return new AdminContact("contact@koreatech.ac.kr");
    }

    /**
     * 관리자 이메일을 수정한다.
     *
     * @param email 새 이메일 주소 (null 또는 빈 문자열이면 무시)
     */
    public void updateEmail(String email) {
        if (isInvalidEmail(email)) {
            return;
        }
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    private boolean isInvalidEmail(String email) {
        return email == null || email.isBlank();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
