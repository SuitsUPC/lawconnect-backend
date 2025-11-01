package com.qu3dena.lawconnect.backend.iam.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.iam.domain.model.aggregates.UserAggregate;
import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetAllUsersQuery;
import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetUserByIdQuery;
import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetUserByUsernameQuery;
import com.qu3dena.lawconnect.backend.iam.domain.services.UserQueryService;
import com.qu3dena.lawconnect.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link UserQueryService} for handling user-related queries.
 * <p>
 * Provides methods to retrieve users from the repository based on different query criteria.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    /**
     * Creates a new instance of {@code UserQueryServiceImpl} with the required user repository.
     *
     * @param userRepository repository for user persistence operations
     */
    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserAggregate> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserAggregate> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.id());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserAggregate> handle(GetUserByUsernameQuery query) {
        return userRepository.findByUsername(query.username());
    }
}
