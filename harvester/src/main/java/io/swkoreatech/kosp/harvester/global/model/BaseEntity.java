package io.swkoreatech.kosp.harvester.global.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(builderMethodName = "baseBuilder")
@RequiredArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = true)
    private LocalDateTime updatedAt;

}
