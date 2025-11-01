package com.qu3dena.lawconnect.backend.iam.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.iam.domain.model.aggregates.UserAggregate;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources.UserResource;

/**
 * Assembler class for converting {@link UserAggregate} entities into {@link UserResource} objects.
 * <p>
 * Used to transform domain user aggregates into resources suitable for API responses.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
public class UserResourceFromEntityAssembler {

    /**
     * Converts a {@link UserAggregate} entity into a {@link UserResource}.
     *
     * @param entity the user aggregate entity to convert
     * @return the corresponding {@link UserResource}
     */
    public static UserResource toResourceFromEntity(UserAggregate entity) {
        return new UserResource(
                entity.getId(),
                entity.getUsername(),
                entity.getRoleName()
        );
    }
}
