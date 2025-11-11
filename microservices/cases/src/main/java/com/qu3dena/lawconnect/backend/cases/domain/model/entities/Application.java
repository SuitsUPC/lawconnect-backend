package com.qu3dena.lawconnect.backend.cases.domain.model.entities;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.ApplicationStatus;
import com.qu3dena.lawconnect.backend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entity representing an application in the system.
 * <p>
 * This class is used to manage applications associated with cases and lawyers.
 * It extends {@link AuditableModel} to include audit information such as creation
 * and modification timestamps.
 *
 * <p>Each application is linked to a specific case and lawyer, and has a status
 * represented by {@link ApplicationStatus}.</p>
 *
 * <p>Use this entity to persist and retrieve application data from the database.</p>
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "applications")
@EqualsAndHashCode(callSuper = true, exclude = "legalCase")
public class Application extends AuditableModel {

    /**
     * Unique identifier for the application.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The case associated with the application.
     */
    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private CaseAggregate legalCase;

    /**
     * The unique identifier of the lawyer associated with the application.
     */
    @Column(name = "lawyer_id", nullable = false)
    private UUID lawyerId;

    /**
     * The status of the application.
     */
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    /**
     * The message or cover letter submitted by the lawyer with the application.
     */
    @Column(name = "message", length = 1000)
    private String message;

    public Application(CaseAggregate legalCase, UUID lawyerId, ApplicationStatus status, String message) {
        this.legalCase = legalCase;
        this.lawyerId = lawyerId;
        this.status = status;
        this.message = message;
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
     * Creates a new Application instance with the specified case, lawyer ID, status, and message.
     *
     * @param legalCase the case associated with the application
     * @param lawyerId  the unique identifier of the lawyer
     * @param status    the status of the application
     * @param message   the cover letter or message from the lawyer
     * @return a new Application instance
     */
    public static Application create(CaseAggregate legalCase, UUID lawyerId, ApplicationStatus status, String message) {
        return new Application(legalCase, lawyerId, status, message);
    }
}
