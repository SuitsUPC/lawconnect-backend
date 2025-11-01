package com.qu3dena.lawconnect.backend.iam.domain.services;

import com.qu3dena.lawconnect.backend.iam.domain.model.aggregates.UserAggregate;
import com.qu3dena.lawconnect.backend.iam.domain.model.commands.SignInCommand;
import com.qu3dena.lawconnect.backend.iam.domain.model.commands.SignUpCommand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

/**
 * Service interface for handling user-related commands such as sign-up and sign-in.
 * <p>
 * Provides methods to process user registration and authentication commands.
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
public interface UserCommandService {

    /**
     * Handles the user sign-up command.
     *
     * @param command the sign-up command containing user registration details
     * @return an {@link Optional} containing the created {@link UserAggregate} if successful, or empty if not
     */
    Optional<UserAggregate> handle(SignUpCommand command);

    /**
     * Handles the user sign-in command.
     *
     * @param command the sign-in command containing user authentication details
     * @return an {@link Optional} containing an {@link ImmutablePair} of the authenticated {@link UserAggregate}
     * and a generated authentication token if successful, or empty if authentication fails
     */
    Optional<ImmutablePair<UserAggregate, String>> handle(SignInCommand command);
}
