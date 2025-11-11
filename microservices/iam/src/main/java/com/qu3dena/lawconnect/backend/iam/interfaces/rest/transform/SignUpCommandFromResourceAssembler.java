package com.qu3dena.lawconnect.backend.iam.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.iam.domain.model.commands.SignUpCommand;
import com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects.Roles;
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
        // Convert role string to enum, adding ROLE_ prefix if not present
        String roleName = resource.role().startsWith("ROLE_") ? resource.role() : "ROLE_" + resource.role();
        var roleEnum = Roles.valueOf(roleName);

        return new SignUpCommand(
                resource.username(),
                resource.password(),
                roleEnum
        );
    }
}
