package com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources;

import java.util.UUID;

/**
 * Resource representing an authenticated user with their ID, username, and authentication token.
 * <p>
 * Used as a response object after successful authentication.
 * </p>
 *
 * @param id       the unique identifier of the user
 * @param username the username of the authenticated user
 * @param token    the authentication token issued to the user
 * @author LawConnect Team
 * @since 1.0.0
 */
public record AuthenticatedUserResource(UUID id, String username, String token) {
}
