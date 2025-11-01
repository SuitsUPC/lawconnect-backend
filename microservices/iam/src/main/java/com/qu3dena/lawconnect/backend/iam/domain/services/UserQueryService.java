package com.qu3dena.lawconnect.backend.iam.domain.services;

import com.qu3dena.lawconnect.backend.iam.domain.model.aggregates.UserAggregate;
import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetAllUsersQuery;
import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetUserByIdQuery;
import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetUserByUsernameQuery;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling user-related query operations.
 * <p>
 * Provides methods to retrieve users based on different query criteria.
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
public interface UserQueryService {

    /**
     * Handles the query to retrieve all users.
     *
     * @param query the query object for retrieving all users
     * @return a list of all {@link UserAggregate} instances
     */
    List<UserAggregate> handle(GetAllUsersQuery query);

    /**
     * Handles the query to retrieve a user by their unique identifier.
     *
     * @param query the query object containing the user ID
     * @return an {@link Optional} containing the found {@link UserAggregate}, or empty if not found
     */
    Optional<UserAggregate> handle(GetUserByIdQuery query);

    /**
     * Handles the query to retrieve a user by their username.
     *
     * @param query the query object containing the username
     * @return an {@link Optional} containing the found {@link UserAggregate}, or empty if not found
     */
    Optional<UserAggregate> handle(GetUserByUsernameQuery query);
}
