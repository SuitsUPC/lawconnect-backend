package com.qu3dena.lawconnect.backend.iam.domain.model.commands;

import com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects.Roles;

/**
 * Command representing a user sign-up request.
 * <p>
 * Encapsulates the username, password, role, and personal information for user registration.
 *
 * @param username the username of the new user
 * @param password the password of the new user
 * @param role     the role assigned to the new user
 * @param firstname the first name of the new user
 * @param lastname the last name of the new user
 * @param phoneNumber the phone number of the new user
 * @param dni the DNI (documento nacional de identidad) of the new user
 */
public record SignUpCommand(
        String username,
        String password,
        Roles role,
        String firstname,
        String lastname,
        String phoneNumber,
        String dni
) {
}
