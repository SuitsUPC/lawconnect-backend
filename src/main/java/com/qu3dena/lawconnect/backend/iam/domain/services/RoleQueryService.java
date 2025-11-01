package com.qu3dena.lawconnect.backend.iam.domain.services;

import com.qu3dena.lawconnect.backend.iam.domain.model.entities.Role;
import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetAllRolesQuery;
import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetRoleByNameQuery;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling role-related query operations.
 * <p>
 * Provides methods to retrieve roles based on different query criteria.
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
public interface RoleQueryService {

    /**
     * Handles the query to retrieve all roles.
     *
     * @param query the query object for retrieving all roles
     * @return a list of all {@link Role} instances
     */
    List<Role> handle(GetAllRolesQuery query);

    /**
     * Handles the query to retrieve a role by its name.
     *
     * @param query the query object containing the role name
     * @return an {@link Optional} containing the found {@link Role}, or empty if not found
     */
    Optional<Role> handle(GetRoleByNameQuery query);
}
