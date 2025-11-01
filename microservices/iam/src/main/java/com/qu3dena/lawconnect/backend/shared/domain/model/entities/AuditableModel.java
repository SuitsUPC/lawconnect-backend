package com.qu3dena.lawconnect.backend.shared.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * Abstract base class for entities that require auditing.
 * <p>
 * Provides automatic management of creation and update timestamps.
 */
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class AuditableModel {
    /**
     * The timestamp when the entity was created.
     * Set automatically and not updatable.
     */
    @Getter
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    /**
     * The timestamp when the entity was last updated.
     * Set automatically.
     */
    @Getter
    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;
}
