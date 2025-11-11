package com.qu3dena.lawconnect.backend.profiles.domain.services;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.ClientAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetAllClientsQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetClientByUserIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling client-related queries.
 * <p>
 * This service defines the contract for processing queries
 * related to client operations, such as retrieving a client by DNI.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface ClientQueryService {

    /**
     * Handles the retrieval of all clients.
     *
     * @param query the query to retrieve all clients
     * @return a {@link List} of {@link ClientAggregate}
     */
    List<ClientAggregate> handle(GetAllClientsQuery query);

    /**
     * Handles the retrieval of a client by user ID.
     *
     * @param query the query containing the user ID
     * @return an {@link Optional} containing the retrieved {@link ClientAggregate}, or empty if not found
     */
    Optional<ClientAggregate> handle(GetClientByUserIdQuery query);
}
