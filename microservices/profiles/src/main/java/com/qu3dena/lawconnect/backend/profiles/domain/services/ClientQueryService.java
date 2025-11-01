package com.qu3dena.lawconnect.backend.profiles.domain.services;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.ClientAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetClientByUserIdQuery;

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

    Optional<ClientAggregate> handle(GetClientByUserIdQuery query);
}
