package com.qu3dena.lawconnect.backend.profiles.domain.services;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.LawyerAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.CreateLawyerCommand;

import java.util.Optional;

/**
 * Service interface for handling lawyer-related commands.
 * <p>
 * This service defines the contract for processing commands
 * related to lawyer operations, such as creating a new lawyer.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface LawyerCommandService {

    /**
     * Handles the creation of a new lawyer.
     *
     * @param command the command containing the details for creating a lawyer
     * @return an {@link Optional} containing the created {@link LawyerAggregate}, or empty if creation fails
     */
    Optional<LawyerAggregate> handle(CreateLawyerCommand command);
}
