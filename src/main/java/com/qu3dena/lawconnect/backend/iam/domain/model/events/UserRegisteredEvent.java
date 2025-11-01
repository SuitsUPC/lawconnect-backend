package com.qu3dena.lawconnect.backend.iam.domain.model.events;

import java.util.UUID;

/**
 * Domain event representing the registration of a new user.
 * <p>
 * Contains the unique identifier of the registered user.
 *
 * @param userId the unique identifier of the registered user
 */
public record UserRegisteredEvent(
        UUID userId
) {
}
