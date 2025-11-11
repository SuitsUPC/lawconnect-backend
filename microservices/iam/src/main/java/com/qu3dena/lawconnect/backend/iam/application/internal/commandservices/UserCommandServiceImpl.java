package com.qu3dena.lawconnect.backend.iam.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.iam.application.internal.outboundservices.hashing.HashingService;
import com.qu3dena.lawconnect.backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.qu3dena.lawconnect.backend.iam.domain.model.aggregates.UserAggregate;
import com.qu3dena.lawconnect.backend.iam.domain.model.commands.SignInCommand;
import com.qu3dena.lawconnect.backend.iam.domain.model.commands.SignUpCommand;
import com.qu3dena.lawconnect.backend.iam.domain.model.events.UserRegisteredEvent;
import com.qu3dena.lawconnect.backend.iam.domain.services.UserCommandService;
import com.qu3dena.lawconnect.backend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.qu3dena.lawconnect.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of {@link UserCommandService} for handling user-related commands.
 * <p>
 * Provides methods for user registration and authentication, including password hashing,
 * role assignment, and token generation.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final TokenService tokenService;
    private final HashingService hashingService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher events;

    /**
     * Creates a new instance of {@code UserCommandServiceImpl} with the required dependencies.
     *
     * @param userRepository repository for user operations
     * @param roleRepository repository for role operations
     * @param tokenService   service for token generation
     * @param hashingService service for password validation and hashing
     * @param events         publisher for application events
     */
    public UserCommandServiceImpl(UserRepository userRepository, RoleRepository roleRepository, TokenService tokenService, HashingService hashingService, ApplicationEventPublisher events) {
        this.events = events;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Optional<UserAggregate> handle(SignUpCommand command) {

        if (userRepository.existsByUsername(command.username()))
            throw new RuntimeException("Username already exists");

        var encoded = hashingService.encode(command.password());

        var roleEntity = roleRepository.findByName(command.role())
                .orElseThrow(() -> new RuntimeException("Role not found: " + command.role()));

        var user = UserAggregate.create(command.username(), encoded, roleEntity);

        var saved = userRepository.save(user);

        var event = new UserRegisteredEvent(saved.getId());

        events.publishEvent(event);

        return Optional.of(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ImmutablePair<UserAggregate, String>> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());

        if (user.isEmpty())
            throw new RuntimeException("User not found");

        var existingUser = user.get();

        if (!hashingService.matches(command.password(), existingUser.getPassword()))
            throw new RuntimeException("Invalid password");

        var token = tokenService.generateToken(existingUser.getId().toString());

        return Optional.of(ImmutablePair.of(existingUser, token));
    }
}
