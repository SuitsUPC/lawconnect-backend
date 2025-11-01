package com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources;

/**
 * Resource representing the credentials required for user sign-in.
 * <p>
 * Used as a request object for authentication endpoints.
 * </p>
 *
 * @param username the username of the user attempting to sign in
 * @param password the password of the user attempting to sign in
 * @author LawConnect Team
 * @since 1.0.0
 */
public record SignInResource(String username, String password) {
}
