package com.qu3dena.lawconnect.backend.cases.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.AcceptInvitationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.InviteLawyerCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.RejectInvitationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Invitation;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.InvitationAcceptedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.InvitationRejectedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.LawyerInvitedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.InvitationStatus;
import com.qu3dena.lawconnect.backend.cases.domain.services.InvitationCommandService;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CaseRepository;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.InvitationRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for handling invitation-related commands.
 * </p>
 * Provides methods to invite lawyers, accept invitations, and reject invitations,
 * while ensuring business rules and domain constraints are respected.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
public class InvitationCommandServiceImpl implements InvitationCommandService {

    private final CaseRepository caseRepository;
    private final InvitationRepository invitationRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructs an instance of {@link InvitationCommandServiceImpl}.
     *
     * @param caseRepository       the repository for managing legal cases
     * @param invitationRepository the repository for managing invitations
     * @param eventPublisher       the event publisher for domain events
     */
    public InvitationCommandServiceImpl(CaseRepository caseRepository, InvitationRepository invitationRepository, ApplicationEventPublisher eventPublisher) {
        this.caseRepository = caseRepository;
        this.invitationRepository = invitationRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles the rejection of an invitation.
     * Validates the invitation and lawyer ID, updates the invitation's status to REJECTED,
     * and publishes an event. If there are no pending invitations or applications, reopens the case.
     *
     * @param command the command containing the details for rejecting the invitation
     * @return an {@link Optional} containing the rejected invitation, if successful
     */
    @Override
    @Transactional
    public Optional<Invitation> handle(RejectInvitationCommand command) {
        return handleInvitation(command.invitationId(), command.lawyerId(), InvitationStatus.REJECTED);
    }

    /**
     * Handles the acceptance of an invitation.
     * Validates the invitation and lawyer ID, updates the invitation's status to ACCEPTED,
     * updates the associated case's status, and publishes an event.
     *
     * @param command the command containing the details for accepting the invitation
     * @return an {@link Optional} containing the accepted invitation, if successful
     */
    @Override
    @Transactional
    public Optional<Invitation> handle(AcceptInvitationCommand command) {
        return handleInvitation(command.invitationId(), command.lawyerId(), InvitationStatus.ACCEPTED);
    }

    /**
     * Handles the invitation of a lawyer to a case.
     * Validates the case status, ensures the lawyer has not already been invited,
     * creates a new invitation, updates the case's status if necessary, and publishes an event.
     *
     * @param command the command containing the details for inviting the lawyer
     * @return an {@link Optional} containing the created invitation, if successful
     */
    @Override
    @Transactional
    public Optional<Invitation> handle(InviteLawyerCommand command) {

        // 1). Check if the case exists and is in the OPEN status
        var maybeCase = caseRepository.findById(command.caseId())
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // 2). If the case is not OPEN, throw an exception
        if (maybeCase.getStatus() == CaseStatus.OPEN) {
            maybeCase.evaluation();
            caseRepository.save(maybeCase);
        }

        // 3). Check if the lawyer has already been invited to this case
        var alreadyInvited = invitationRepository
                .findByLawyerIdAndLegalCase_Id(command.lawyerId(), command.caseId());

        // 4). If the lawyer has already been invited, throw an exception
        if (alreadyInvited.isPresent())
            throw new RuntimeException("Lawyer has already been invited to this case");

        // 5). Create a new invitation and save it
        var invitation = Invitation.create(maybeCase, command.lawyerId(), InvitationStatus.PENDING);
        var saved = invitationRepository.save(invitation);

        // 6). Publish an event indicating that a lawyer has been invited
        eventPublisher.publishEvent(new LawyerInvitedEvent(
                saved.getCaseId(),
                saved.getId(),
                saved.getClientId(),
                saved.getLawyerId()
        ));

        return Optional.of(saved);
    }

    /**
     * Handles the processing of an invitation.
     * Validates the invitation and lawyer ID, updates the invitation's status,
     * updates the associated case's status if necessary, and publishes an event.
     *
     * @param invitationId the ID of the invitation to process
     * @param lawyerId     the ID of the lawyer associated with the invitation
     * @param status       the new status of the invitation
     * @return an {@link Optional} containing the processed invitation, if successful
     */
    private Optional<Invitation> handleInvitation(Long invitationId, UUID lawyerId, InvitationStatus status) {

        // 1). Validate the invitation and lawyer ID
        var maybeInvitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        // 2). Check if the lawyer ID matches the one in the invitation
        if (!lawyerId.equals(maybeInvitation.getLawyerId()))
            throw new RuntimeException("Invitation id mismatch");

        // 3). Get the associated legal case
        var invitationCase = maybeInvitation.getLegalCase();

        // 4). Check if the case is in EVALUATION status
        if (invitationCase.getStatus() != CaseStatus.EVALUATION)
            throw new IllegalStateException("Invitations can only be accepted/rejected when case is in EVALUATION");

        // 5). Update the invitation status and save it
        maybeInvitation.setStatus(status);
        var saved = invitationRepository.save(maybeInvitation);

        // 6). Handle the case based on the invitation status
        if (status == InvitationStatus.ACCEPTED) {
            invitationCase.accept(saved.getLawyerId());
            caseRepository.save(invitationCase);

            eventPublisher.publishEvent(new InvitationAcceptedEvent(
                    saved.getCaseId(),
                    saved.getId(),
                    saved.getLawyerId()
            ));
        } else if (status == InvitationStatus.REJECTED) {

            if (invitationCase.hasNoPendingInvitationsOrApplications()) {
                invitationCase.reopen();
                caseRepository.save(invitationCase);
            }

            eventPublisher.publishEvent(new InvitationRejectedEvent(
                    saved.getCaseId(),
                    saved.getId(),
                    saved.getLawyerId()
            ));
        }

        return Optional.of(saved);
    }
}
