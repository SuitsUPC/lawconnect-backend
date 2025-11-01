package com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources;

/**
 * Resource representing the information required for user sign-up.
 * <p>
 * Used as a request object for registration endpoints.
 * </p>
 *
 * @param username the username chosen by the user during sign-up
 * @param password the password chosen by the user during sign-up
 * @param role     the role assigned to the user during sign-up
 * @author LawConnect Team
 * @since 1.0.0
 */
public record SignUpResource(
        String username,
        String password,
        String role
) {
}
