package com.qu3dena.lawconnect.backend.iam.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.iam.domain.model.entities.Role;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources.RoleResource;

/**
 * Assembler class for converting {@link Role} entities into {@link RoleResource} objects.
 * <p>
 * Used to transform domain role entities into resources suitable for API responses.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
public class RoleResourceFromEntityAssembler {

    /**
     * Converts a {@link Role} entity into a {@link RoleResource}.
     *
     * @param entity the role entity to convert
     * @return the corresponding {@link RoleResource}
     */
    public static RoleResource toResourceFromEntity(Role entity) {
        return new RoleResource(
                entity.getId(),
                entity.getStringName());
    }
}
