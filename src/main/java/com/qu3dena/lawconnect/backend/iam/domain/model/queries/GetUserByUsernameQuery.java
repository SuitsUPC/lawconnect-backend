package com.qu3dena.lawconnect.backend.iam.domain.model.queries;

/**
 * Query to retrieve a user by their username from the system.
 * <p>
 * Used to request a specific user based on the provided username.
 *
 * @param username the username of the user to retrieve
 * @author LawConnect Team
 * @since 1.0.0
 */
public record GetUserByUsernameQuery(String username) {
    public GetUserByUsernameQuery {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be null or blank");
    }
}
