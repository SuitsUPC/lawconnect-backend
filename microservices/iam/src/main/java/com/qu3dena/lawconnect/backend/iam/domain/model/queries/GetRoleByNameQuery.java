package com.qu3dena.lawconnect.backend.iam.domain.model.queries;

import com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects.Roles;

/**
 * Query to retrieve a role by its name from the system.
 * <p>
 * Used to request a specific role based on the provided role name.
 *
 * @param name the name of the role to retrieve
 * @author LawConnect Team
 * @since 1.0.0
 */
public record GetRoleByNameQuery(Roles name) {
}
