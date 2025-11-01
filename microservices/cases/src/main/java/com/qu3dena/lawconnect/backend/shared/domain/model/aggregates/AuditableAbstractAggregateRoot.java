package com.qu3dena.lawconnect.backend.shared.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.UUID;

/**
 * Abstract base class for aggregate roots that require auditing.
 * <p>
 * Provides automatic management of creation and update timestamps,
 * as well as a generated primary key.
 *
 * @param <T> the type of the aggregate root
 */
@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class AuditableAbstractAggregateRoot<T extends AbstractAggregateRoot<T>>
        extends AbstractAggregateRoot<T> {

    /**
     * The unique identifier for the aggregate root.
     */
    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    /**
     * The timestamp when the entity was created.
     * Set automatically and not updatable.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    /**
     * The timestamp when the entity was last updated.
     * Set automatically.
     */
    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;

}
