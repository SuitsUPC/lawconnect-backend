package com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources;

import java.util.UUID;

/**
 * Resource representing a user with their ID, username, and role.
 * <p>
 * Used as a response object for user-related endpoints.
 * </p>
 *
 * @param id       the unique identifier of the user
 * @param username the username of the user
 * @param role     the role assigned to the user
 * @since 1.0.0
 * @author LawConnect Team
 */
public record UserResource(UUID id, String username, String role) {
}
