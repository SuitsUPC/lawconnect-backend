package com.qu3dena.lawconnect.backend.iam.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.iam.domain.model.aggregates.UserAggregate;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources.AuthenticatedUserResource;

/**
 * Assembler class for converting {@link UserAggregate} entities and JWT tokens
 * into {@link AuthenticatedUserResource} objects.
 * <p>
 * Used to transform domain entities into resources suitable for API responses
 * that include authentication information.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
public class AuthenticatedUserResourceFromEntityAssembler {

    /**
     * Converts a {@link UserAggregate} entity and a JWT token into an {@link AuthenticatedUserResource}.
     *
     * @param entity the user aggregate entity to convert
     * @param token  the JWT authentication token
     * @return the corresponding {@link AuthenticatedUserResource}
     */
    public static AuthenticatedUserResource toResourceFromEntity(UserAggregate entity, String token) {
        return new AuthenticatedUserResource(
                entity.getId(),
                entity.getUsername(),
                token
        );
    }
}
