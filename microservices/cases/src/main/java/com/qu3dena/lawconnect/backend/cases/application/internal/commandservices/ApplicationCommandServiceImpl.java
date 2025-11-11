package com.qu3dena.lawconnect.backend.cases.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.AcceptApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.RejectApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.SubmitApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Application;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.ApplicationAcceptedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.ApplicationRejectedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.ApplicationSubmittedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.ApplicationStatus;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.services.ApplicationCommandService;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.ApplicationRepository;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CaseRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service implementation for handling application-related commands.
 * </p>
 * Provides methods to accept, submit, and reject applications, while ensuring
 * business rules and domain constraints are respected.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
public class ApplicationCommandServiceImpl implements ApplicationCommandService {

    private final CaseRepository caseRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructs an instance of {@link ApplicationCommandServiceImpl}.
     *
     * @param caseRepository        the repository for managing legal cases
     * @param applicationRepository the repository for managing applications
     * @param eventPublisher        the event publisher for domain events
     */
    public ApplicationCommandServiceImpl(CaseRepository caseRepository, ApplicationRepository applicationRepository, ApplicationEventPublisher eventPublisher) {
        this.caseRepository = caseRepository;
        this.applicationRepository = applicationRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles the acceptance of an application.
     * Validates the client ID, updates the application's status to ACCEPTED,
     * updates the associated case's status, and publishes an event.
     *
     * @param command the command containing the details for accepting the application
     * @return an {@link Optional} containing the accepted application, if successful
     */
    @Override
    public Optional<Application> handle(AcceptApplicationCommand command) {

        // 1). Retrieve the application by its ID or throw an exception if not found
        var maybeApplication = applicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new IllegalArgumentException("application not found"));

        // 2). Validate that the client ID in the command matches the client ID of the application's case
        if (!command.clientId().equals(maybeApplication.getClientId()))
            throw new IllegalArgumentException("client id mismatch");

        // 3). Update the application's status to ACCEPTED and save the changes
        maybeApplication.setStatus(ApplicationStatus.ACCEPTED);
        var saved = applicationRepository.save(maybeApplication);

        // 4). Update the associated case's status to ACCEPTED and save the changes
        var applicationCase = saved.getLegalCase();
        applicationCase.accept(saved.getLawyerId());
        caseRepository.save(applicationCase);

        // 5). Publish an event indicating that the application has been accepted
        eventPublisher.publishEvent(new ApplicationAcceptedEvent(
                saved.getCaseId(),
                saved.getId(),
                saved.getClientId()
        ));

        return Optional.of(saved);
    }

    /**
     * Handles the submission of an application.
     * Validates the case status, updates the case's status to EVALUATION,
     * creates a new application, and publishes an event.
     *
     * @param command the command containing the details for submitting the application
     * @return an {@link Optional} containing the submitted application, if successful
     */
    @Override
    public Optional<Application> handle(SubmitApplicationCommand command) {

        // 1). Retrieve the case by its ID or throw an exception if not found
        var maybeCase = caseRepository.findById(command.caseId())
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));

        // 2). Validate that the case status is OPEN before proceeding
        if (maybeCase.getStatus() != CaseStatus.OPEN)
            throw new IllegalStateException("Applications can only be submitted to OPEN cases");

        // 3). Update the case's status to EVALUATION and save the changes
        maybeCase.evaluation();
        caseRepository.save(maybeCase);

        // 4). Create a new application, save it, and return the saved instance
        var application = Application.create(maybeCase, command.lawyerId(), ApplicationStatus.SUBMITTED, command.message());
        var saved = applicationRepository.save(application);

        // 5). Publish an event indicating that the application has been submitted
        eventPublisher.publishEvent(new ApplicationSubmittedEvent(
                saved.getCaseId(),
                saved.getId(),
                saved.getLawyerId()
        ));

        return Optional.of(saved);
    }

    /**
     * Handles the rejection of an application.
     * Validates the client ID and case status, updates the application's status to REJECTED,
     * reopens the case if there are no pending invitations or applications, and publishes an event.
     *
     * @param command the command containing the details for rejecting the application
     * @return an {@link Optional} containing the rejected application, if successful
     */
    @Override
    @Transactional
    public Optional<Application> handle(RejectApplicationCommand command) {

        // 1). Retrieve the application by its ID or throw an exception if not found
        var maybeApplication = applicationRepository.findById(command.applicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // 2). Validate that the client ID in the command matches the client ID of the application's case
        if (!command.clientId().equals(maybeApplication.getClientId()))
            throw new IllegalArgumentException("client id mismatch");

        // 3). Validate that the application's case is in EVALUATION status before proceeding
        var applicationCase = maybeApplication.getLegalCase();
        if (applicationCase.getStatus() != CaseStatus.EVALUATION)
            throw new IllegalStateException("Applications can only be accepted/rejected when case is in EVALUATION");

        // 4). Update the application's status to REJECTED, save it, and check if the case has no pending invitations or applications
        maybeApplication.setStatus(ApplicationStatus.REJECTED);
        var saved = applicationRepository.save(maybeApplication);

        // 5). If the case has no pending invitations or applications, reopen it
        if (applicationCase.hasNoPendingInvitationsOrApplications()) {
            applicationCase.reopen();
            caseRepository.save(applicationCase);
        }

        // 6). Publish an event indicating that the application has been rejected
        eventPublisher.publishEvent(new ApplicationRejectedEvent(
                saved.getCaseId(),
                saved.getId(),
                saved.getClientId()
        ));

        return Optional.of(saved);
    }
}
