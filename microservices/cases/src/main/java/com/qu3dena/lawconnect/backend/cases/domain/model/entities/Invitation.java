package com.qu3dena.lawconnect.backend.cases.domain.model.entities;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.InvitationStatus;
import com.qu3dena.lawconnect.backend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entity representing an invitation in the system.
 * <p>
 * This class is used to manage invitations associated with cases and lawyers.
 * It extends {@link AuditableModel} to include audit information such as creation
 * and modification timestamps.
 *
 * <p>Each invitation is linked to a specific case and lawyer, and has a status
 * represented by {@link InvitationStatus}.</p>
 *
 * <p>Use this entity to persist and retrieve invitation data from the database.</p>
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "invitations")
@EqualsAndHashCode(callSuper = true, exclude = "legalCase")
public class Invitation extends AuditableModel {

    /**
     * Unique identifier for the invitation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The case associated with the invitation.
     */
    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private CaseAggregate legalCase;

    /**
     * The unique identifier of the lawyer associated with the invitation.
     */
    @Column(name = "lawyer_id", nullable = false)
    private UUID lawyerId;

    /**
     * The status of the invitation.
     */
    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    public Invitation(CaseAggregate legalCase, UUID lawyerId, InvitationStatus status) {
        this.legalCase = legalCase;
        this.lawyerId = lawyerId;
        this.status = status;
    }

    /**
     * Retrieves the unique identifier of the case associated with this application.
     *
     * @return the unique identifier of the case
     */
    public UUID getCaseId() {
        return legalCase.getId();
    }

    /**
     * Retrieves the unique identifier of the client associated with the case of this application.
     *
     * @return the unique identifier of the client
     */
    public UUID getClientId() {
        return legalCase.getClientId();
    }

    /**
     * Creates a new Invitation instance with the specified case, lawyer ID, and status.
     *
     * @param legalCase the case associated with the invitation
     * @param lawyerId  the unique identifier of the lawyer
     * @param status    the status of the invitation
     * @return a new Invitation instance
     */
    public static Invitation create(CaseAggregate legalCase, UUID lawyerId, InvitationStatus status) {
        return new Invitation(legalCase, lawyerId, status);
    }
}
