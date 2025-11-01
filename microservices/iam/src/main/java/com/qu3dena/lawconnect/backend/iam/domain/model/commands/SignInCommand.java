package com.qu3dena.lawconnect.backend.iam.domain.model.commands;

/**
 * Command representing a user sign-in request.
 * <p>
 * Encapsulates the username and password required for authentication.
 * Performs validation to ensure neither field is null or blank.
 *
 * @param username the username of the user attempting to sign in
 * @param password the password of the user attempting to sign in
 */
public record SignInCommand(String username, String password) {
    public SignInCommand {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be null or blank");

        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password cannot be null or blank");
    }
}
