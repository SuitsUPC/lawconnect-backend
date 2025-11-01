package com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources;

/**
 * Resource representing a role with its ID and name.
 * <p>
 * Used as a response object for role-related endpoints.
 * </p>
 *
 * @param id   the unique identifier of the role
 * @param name the name of the role
 * @author LawConnect Team
 * @since 1.0.0
 */
public record RoleResource(Long id, String name) {
}
