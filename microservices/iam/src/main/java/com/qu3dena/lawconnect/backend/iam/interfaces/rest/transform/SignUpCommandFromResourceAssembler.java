package com.qu3dena.lawconnect.backend.iam.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.iam.domain.model.commands.SignUpCommand;
import com.qu3dena.lawconnect.backend.iam.domain.model.entities.Role;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources.SignUpResource;

/**
 * Assembler class for converting {@link SignUpResource} objects into {@link SignUpCommand} commands.
 * <p>
 * Used to transform API request resources into domain commands for user registration operations.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
public class SignUpCommandFromResourceAssembler {

    /**
     * Converts a {@link SignUpResource} into a {@link SignUpCommand}.
     *
     * @param resource the sign-up resource containing user registration data
     * @return a new {@link SignUpCommand} constructed from the provided resource
     */
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var roleEntity = Role.toRoleFromName(resource.role());

        System.out.println("Role Entity: " + roleEntity);

        return new SignUpCommand(
                resource.username(),
                resource.password(),
                roleEntity
        );
    }
}
