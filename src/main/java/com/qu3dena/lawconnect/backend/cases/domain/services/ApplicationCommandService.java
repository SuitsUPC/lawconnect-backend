package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.AcceptApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.RejectApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.SubmitApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Application;

import java.util.Optional;

/**
 * Service interface for handling application-related commands.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface ApplicationCommandService {

    /**
     * Handles the acceptance of an application.
     *
     * @param command the command containing the details of the application to be accepted
     */
    Optional<Application>  handle(AcceptApplicationCommand command);

    /**
     * Handles the submission of an application.
     *
     * @param command the command containing the details of the application to be submitted
     */
    Optional<Application> handle(SubmitApplicationCommand command);

    /**
     * Handles the rejection of an application.
     *
     * @param command the command containing the details of the application to be rejected
     * @return an Optional containing the rejected Application if successful, or empty if not
     */
    Optional<Application> handle(RejectApplicationCommand command);
}
