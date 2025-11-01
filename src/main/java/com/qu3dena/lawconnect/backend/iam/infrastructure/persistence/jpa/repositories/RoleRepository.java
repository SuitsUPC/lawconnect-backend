package com.qu3dena.lawconnect.backend.iam.infrastructure.persistence.jpa.repositories;

import com.qu3dena.lawconnect.backend.iam.domain.model.entities.Role;
import com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link Role} entities.
 * <p>
 * Provides methods to perform CRUD operations and custom queries for roles in the database.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its name.
     *
     * @param name the {@link Roles} value object representing the role name
     * @return an {@link Optional} containing the found {@link Role}, or empty if not found
     */
    Optional<Role> findByName(Roles name);

    /**
     * Checks if a role exists by its name.
     *
     * @param name the {@link Roles} value object representing the role name
     * @return {@code true} if a role with the given name exists, {@code false} otherwise
     */
    boolean existsByName(Roles name);
}
