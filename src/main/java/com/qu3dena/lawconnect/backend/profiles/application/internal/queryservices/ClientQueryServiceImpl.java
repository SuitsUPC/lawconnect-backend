package com.qu3dena.lawconnect.backend.profiles.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.ClientAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetClientByUserIdQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.services.ClientQueryService;
import com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa.repositories.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of the {@link ClientQueryService} interface.
 * <p>
 * This service handles queries related to client operations,
 * such as retrieving a client by their DNI.
 * It interacts with the {@link ClientRepository} to fetch data.
 *
 * @author GonzaloQu\
 * @since 1.0
 */
@Service
public class ClientQueryServiceImpl implements ClientQueryService {

    private final ClientRepository clientRepository;

    /**
     * Constructs a new instance of {@code ClientQueryServiceImpl}.
     *
     * @param clientRepository the repository for managing client entities
     */
    public ClientQueryServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ClientAggregate> handle(GetClientByUserIdQuery query) {
        return clientRepository.findByUserId(query.userId());
    }
}
