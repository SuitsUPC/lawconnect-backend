package com.qu3dena.lawconnect.backend.profiles.domain.services;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.ClientAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.CreateClientCommand;
import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.UpdateClientProfileCommand;

import java.util.Optional;

/**
 * Service interface for handling client-related commands.
 * <p>
 * This service defines the contract for processing commands
 * related to client operations, such as creating a new client.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface ClientCommandService {

    /**
     * Handles the creation of a new client.
     *
     * @param command the command containing the details for creating a client
     * @return an {@link Optional} containing the created {@link ClientAggregate}, or empty if creation fails
     */
    Optional<ClientAggregate> handle(CreateClientCommand command);

    /**
     * Handles the update of an existing client profile.
     *
     * @param command the command containing the updated details for the client
     * @return an {@link Optional} containing the updated {@link ClientAggregate}, or empty if update fails
     */
    Optional<ClientAggregate> handle(UpdateClientProfileCommand command);
}
