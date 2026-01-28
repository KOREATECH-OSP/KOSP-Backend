package io.swkoreatech.kosp.domain.admin.contact.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    public static AdminContact createDefault() {
        return new AdminContact("contact@koreatech.ac.kr");
    }

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
