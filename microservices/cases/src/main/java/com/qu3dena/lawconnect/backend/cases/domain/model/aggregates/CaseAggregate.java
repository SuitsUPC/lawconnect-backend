package com.qu3dena.lawconnect.backend.cases.domain.model.aggregates;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Application;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.CaseState;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Comment;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Invitation;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.ApplicationStatus;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseTitle;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.InvitationStatus;
import com.qu3dena.lawconnect.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Represents a legal case aggregate in the system.
 * </p>
 * This class is an aggregate root that manages the lifecycle of a case,
 * including its invitations, applications, comments, and states.
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "cases")
@EqualsAndHashCode(callSuper = true)
public class CaseAggregate extends AuditableAbstractAggregateRoot<CaseAggregate> {

    /**
     * The title of the case.
     */
    @Embedded
    @AttributeOverride(name = "text", column = @Column(name = "title"))
    private CaseTitle title;

    /**
     * The description of the case.
     */
    @Embedded
    @AttributeOverride(name = "text", column = @Column(name = "description"))
    private Description description;

    /**
     * The unique identifier of the client associated with the case.
     */
    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    /**
     * The unique identifier of the lawyer assigned to the case.
     */
    @Column(name = "assigned_lawyer_id")
    private UUID assignedLawyerId;

    /**
     * The ID of the legal specialty required for this case.
     */
    @Column(name = "specialty_id")
    private Long specialtyId;

    /**
     * The set of invitations associated with the case.
     */
    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL)
    private Set<Invitation> invitations = new HashSet<>();

    /**
     * The set of applications associated with the case.
     */
    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL)
    private Set<Application> applications = new HashSet<>();

    /**
     * The list of comments associated with the case.
     */
    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    /**
     * The list of states representing the history of the case's status.
     */
    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL)
    private List<CaseState> states = new ArrayList<>();

    /**
     * The current status of the case.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private CaseStatus currentStatus;

    /**
     * Constructs a new CaseAggregate with the specified client ID, title, description, and specialty ID.
     *
     * @param clientId    the unique identifier of the client
     * @param title       the title of the case
     * @param description the description of the case
     * @param specialtyId the ID of the legal specialty required for this case (nullable)
     */
    public CaseAggregate(UUID clientId, CaseTitle title, Description description, Long specialtyId) {
        this.clientId = clientId;
        this.title = title;
        this.description = description;
        this.specialtyId = specialtyId;
        this.currentStatus = CaseStatus.OPEN;
    }

    /**
     * Creates a new CaseAggregate instance and initializes its state to OPEN.
     *
     * @param clientId    the unique identifier of the client
     * @param title       the title of the case
     * @param description the description of the case
     * @param specialtyId the ID of the legal specialty required for this case (nullable)
     * @return the newly created CaseAggregate
     */
    public static CaseAggregate create(UUID clientId, CaseTitle title, Description description, Long specialtyId) {
        var case_ = new CaseAggregate(clientId, title, description, specialtyId);
        case_.states.add(new CaseState(case_, CaseStatus.OPEN));
        return case_;
    }

    /**
     * Marks the case as closed and updates its status to CLOSED.
     * <p>
     * This method ensures that a case cannot be closed if it is in a CANCELED state.
     * If the current status is valid, the status is updated to CLOSED, and a new
     * {@link CaseState} is added to the case's state history.
     * </p>
     */
    public void close() {
        if (currentStatus != CaseStatus.ACCEPTED)
            throw new IllegalStateException("Only an Accepted case can be closed");

        this.currentStatus = CaseStatus.CLOSED;
        this.states.add(new CaseState(this, CaseStatus.CLOSED));
    }

    /**
     * Marks the case as canceled and updates its status to CANCELED.
     * <p>
     * This method ensures that a case cannot be canceled if it is in a CLOSED state.
     * If the current status is valid, the status is updated to CANCELED, and a new
     * {@link CaseState} is added to the case's state history.
     * </p>
     */
    public void cancel() {
        if (currentStatus != CaseStatus.OPEN && currentStatus != CaseStatus.EVALUATION)
            throw new IllegalStateException("Only an Open or Evaluation case can be canceled");

        this.currentStatus = CaseStatus.CANCELED;
        this.states.add(new CaseState(this, CaseStatus.CANCELED));
    }

    /**
     * Marks the case as under evaluation and updates its status to EVALUATION.
     * <p>
     * This method ensures that a case cannot be evaluated if it is in a CANCELED or CLOSED state.
     * If the current status is valid, the status is updated to EVALUATION, and a new
     * {@link CaseState} is added to the case's state history.
     * </p>
     */
    public void evaluation() {
        if (currentStatus == CaseStatus.CANCELED || currentStatus == CaseStatus.CLOSED)
            throw new IllegalStateException("Cannot evaluate a canceled or closed case");

        this.currentStatus = CaseStatus.EVALUATION;
        this.states.add(new CaseState(this, CaseStatus.EVALUATION));
    }

    /**
     * Marks the case as accepted and updates its status to ACCEPTED.
     * <p>
     * This method ensures that a case cannot be accepted if it is in a CANCELED or CLOSED state.
     * If the current status is valid, the status is updated to ACCEPTED, and a new
     * {@link CaseState} is added to the case's state history.
     * </p>
     */
    public void accept(UUID lawyerId) {
        if (currentStatus == CaseStatus.CANCELED || currentStatus == CaseStatus.CLOSED)
            throw new IllegalStateException("Cannot accept a canceled or closed case");

        this.assignedLawyerId = lawyerId;
        this.currentStatus = CaseStatus.ACCEPTED;
        this.states.add(new CaseState(this, CaseStatus.ACCEPTED));
    }

    /**
     * Reopens the case and updates its status to OPEN.
     * <p>
     * This method ensures that a case can only be reopened if its current status is EVALUATION.
     * If the current status is valid, the status is updated to OPEN, and a new
     * {@link CaseState} is added to the case's state history.
     * </p>
     */
    public void reopen() {
        if (currentStatus != CaseStatus.EVALUATION) {
            throw new IllegalStateException(
                    String.format("Case %s cannot be reopened from status %s", getId(), currentStatus)
            );
        }

        this.currentStatus = CaseStatus.OPEN;
        this.states.add(new CaseState(this, CaseStatus.OPEN));
    }

    /**
     * Checks if the case has no pending invitations or submitted applications.
     * <p>
     * This method evaluates the associated invitations and applications to determine
     * if there are any with a status of PENDING or SUBMITTED, respectively.
     * </p>
     *
     * @return true if there are no pending invitations or submitted applications, false otherwise
     */
    public boolean hasNoPendingInvitationsOrApplications() {

        boolean noPendingInvitations = invitations.stream()
                .noneMatch(inv -> inv.getStatus() == InvitationStatus.PENDING);

        boolean noSubmittedApplications = applications.stream()
                .noneMatch(app -> app.getStatus() == ApplicationStatus.SUBMITTED);

        return noPendingInvitations && noSubmittedApplications;
    }

    /**
     * Retrieves the current status of the case.
     *
     * @return the current status of the case
     * @throws IllegalStateException if no states are available for the case
     */
    public CaseStatus getStatus() {
        if (states.isEmpty())
            throw new IllegalStateException("No states available for this case");

        return states.get(states.size() - 1).getStatus();
    }
}
