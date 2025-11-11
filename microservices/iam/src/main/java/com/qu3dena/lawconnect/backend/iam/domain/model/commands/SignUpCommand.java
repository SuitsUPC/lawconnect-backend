package com.qu3dena.lawconnect.backend.iam.domain.model.commands;

import com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects.Roles;

/**
 * Command representing a user sign-up request.
 * <p>
 * Encapsulates the username, password, and role for user registration.
 *
 * @param username the username of the new user
 * @param password the password of the new user
 * @param role     the role assigned to the new user
 */
public record SignUpCommand(
        String username,
        String password,
        Roles role
) {
}
