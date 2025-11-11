package com.qu3dena.lawconnect.backend.profiles.domain.services;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.LawyerAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.CreateLawyerCommand;
import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.UpdateLawyerSpecialtiesCommand;

import java.util.Optional;

/**
 * Service interface for handling lawyer-related commands.
 * <p>
 * This service defines the contract for processing commands
 * related to lawyer operations, such as creating and updating lawyers.
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

    /**
     * Handles updating a lawyer's specialties.
     *
     * @param command the command containing the user ID and new specialties
     * @return an {@link Optional} containing the updated {@link LawyerAggregate}, or empty if update fails
     */
    Optional<LawyerAggregate> handle(UpdateLawyerSpecialtiesCommand command);
}
