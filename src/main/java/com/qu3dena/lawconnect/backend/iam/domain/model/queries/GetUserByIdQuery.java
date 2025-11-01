package com.qu3dena.lawconnect.backend.iam.domain.model.queries;

import java.util.UUID;

/**
 * Query to retrieve a user by their unique identifier from the system.
 * <p>
 * Used to request a specific user based on the provided user ID.
 *
 * @param id the unique identifier of the user to retrieve
 * @author LawConnect Team
 * @since 1.0.0
 */
public record GetUserByIdQuery(UUID id) {
}
