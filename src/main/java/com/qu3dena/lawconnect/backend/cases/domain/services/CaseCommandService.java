package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CancelCaseCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CloseCaseCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CreateCaseCommand;

import java.util.Optional;

/**
 * Service interface for handling case-related commands.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface CaseCommandService {

    /**
     * Handles the creation of a case.
     *
     * @param command the command containing the details of the case to be created
     */
    Optional<CaseAggregate> handle(CreateCaseCommand command);

    /**
     * Handles the closure of a case.
     *
     * @param command the command containing the details of the case to be closed
     */
    Optional<CaseAggregate> handle(CloseCaseCommand command);

    /**
     * Handles the cancellation of a case.
     *
     * @param command the command containing the details of the case to be canceled
     */
    Optional<CaseAggregate> handle(CancelCaseCommand command);
}
