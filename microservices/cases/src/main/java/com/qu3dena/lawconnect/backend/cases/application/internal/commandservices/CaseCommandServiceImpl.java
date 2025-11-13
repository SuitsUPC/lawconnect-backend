package com.qu3dena.lawconnect.backend.cases.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.events.CaseCanceledEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.CaseClosedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.CaseCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.qu3dena.lawconnect.backend.cases.domain.services.CaseCommandService;
import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CloseCaseCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CancelCaseCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CreateCaseCommand;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CaseRepository;

import java.util.Optional;

/**
 * Service implementation for handling case-related commands.
 * </p>
 * Provides methods to create, close, and cancel cases, while ensuring
 * business rules and domain constraints are respected.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
public class CaseCommandServiceImpl implements CaseCommandService {

    private final CaseRepository caseRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructs an instance of {@link CaseCommandServiceImpl}.
     *
     * @param caseRepository the repository for managing cases
     * @param eventPublisher the event publisher for domain events
     */
    public CaseCommandServiceImpl(CaseRepository caseRepository, ApplicationEventPublisher eventPublisher) {
        this.caseRepository = caseRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles the creation of a new case.
     * Creates a new case aggregate, saves it, and publishes a case created event.
     *
     * @param command the command containing the details for creating the case
     * @return an {@link Optional} containing the created case, if successful
     */
    @Override
    public Optional<CaseAggregate> handle(CreateCaseCommand command) {

        // 1). Create a new case
        var newCase = CaseAggregate.create(
                command.clientId(),
                command.title(),
                command.description(),
                command.specialtyId()
        );

        // 2). Save the new case to the repository
        var saved = caseRepository.save(newCase);

        // 3). Publish a case created event
        eventPublisher.publishEvent(new CaseCreatedEvent(
                saved.getId(),
                saved.getClientId()
        ));

        return Optional.of(saved);
    }

    /**
     * Handles the closing of a case.
     * Validates the client ID, updates the case's status to CLOSED, saves it,
     * and publishes a case closed event.
     *
     * @param command the command containing the details for closing the case
     * @return an {@link Optional} containing the closed case, if successful
     */
    @Override
    public Optional<CaseAggregate> handle(CloseCaseCommand command) {

        // 1). Find the case by ID
        var maybeCase = caseRepository.findById(command.caseId())
                .orElseThrow(() -> new IllegalArgumentException("No case found with id: " + command.caseId()));

        // 2). Validate the client ID is the same as the case owner
        if (!command.clientId().equals(maybeCase.getClientId()))
            throw new IllegalArgumentException("Client ID does not match the case owner");

        // 3). Close the case
        maybeCase.close();

        // 4). Save the updated case
        var saved = caseRepository.save(maybeCase);

        // 5). Publish a case closed event
        eventPublisher.publishEvent(new CaseClosedEvent(
                saved.getId(),
                saved.getClientId()
        ));

        return Optional.of(saved);
    }

    /**
     * Handles the cancellation of a case.
     * Validates the client ID, updates the case's status to CANCELED, saves it,
     * and publishes a case canceled event.
     *
     * @param command the command containing the details for canceling the case
     * @return an {@link Optional} containing the canceled case, if successful
     */
    @Override
    public Optional<CaseAggregate> handle(CancelCaseCommand command) {

        // 1). Find the case by ID
        var maybeCase = caseRepository.findById(command.caseId())
                .orElseThrow(() -> new IllegalArgumentException("No case found with id: " + command.caseId()));

        // 2). Validate the client ID is the same as the case owner
        if (!command.clientId().equals(maybeCase.getClientId()))
            throw new IllegalArgumentException("Client ID does not match the case owner");

        // 3). Cancel the case
        maybeCase.cancel();

        // 4). Save the updated case
        var saved = caseRepository.save(maybeCase);

        // 5). Publish a case canceled event
        eventPublisher.publishEvent(new CaseCanceledEvent(
                saved.getId(),
                saved.getClientId()
        ));

        return Optional.of(saved);
    }
}
