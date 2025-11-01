package com.qu3dena.lawconnect.backend.cases.domain.model.entities;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity representing the state of a case in the system.
 * <p>
 * This class is used to manage the states associated with cases, including
 * their status and the timestamp when the state was recorded.
 * It extends {@link AuditableModel} to include audit information such as creation
 * and modification timestamps.
 *
 * <p>Each case state is linked to a specific case and has a status represented
 * by {@link CaseStatus}.</p>
 *
 * <p>Use this entity to persist and retrieve case state data from the database.</p>
 *
 * @author GonzaloQu\
 * @since 1.0
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "case_states")
@EqualsAndHashCode(callSuper = true)
public class CaseState extends AuditableModel {

    /**
     * Unique identifier for the case state.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The case associated with the state.
     */
    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private CaseAggregate legalCase;

    /**
     * The status of the case at this state.
     */
    @Enumerated(EnumType.STRING)
    private CaseStatus status;

    /**
     * The timestamp when the state was recorded.
     */
    @Column(nullable = false)
    private Instant at;

    /**
     * Constructs a new {@code CaseState} with the specified case and status.
     *
     * @param legalCase  the case associated with the state
     * @param status the status of the case
     */
    public CaseState(CaseAggregate legalCase, CaseStatus status) {
        this.legalCase = legalCase;
        this.status = status;
        this.at = Instant.now();
    }

    /**
     * Factory method to create a new {@code CaseState}.
     *
     * @param case_  the case associated with the state
     * @param status the status of the case
     * @return a new instance of {@code CaseState}
     */
    public static CaseState create(CaseAggregate case_, CaseStatus status) {
        return new CaseState(case_, status);
    }
}
