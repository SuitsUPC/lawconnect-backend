package com.qu3dena.lawconnect.backend.iam.domain.model.entities;

import com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects.Roles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Entity representing a user role within the LawConnect system.
 * <p>
 * Maps to the {@code roles} table and encapsulates the role's unique identifier and name.
 * Provides utility methods for default role assignment, role creation, and validation.
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class Role {

    /**
     * The unique identifier for the role.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the role, represented as an enum.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private Roles name;

    /**
     * Constructs a role with the specified name.
     *
     * @param name the role name
     */
    public Role(Roles name) {
        this.name = name;
    }

    /**
     * Returns the default role, which is {@link Roles#ROLE_CLIENT}.
     *
     * @return the default client role
     */
    public static Role getDefaultRole() {
        return new Role(Roles.ROLE_CLIENT);
    }

    /**
     * Creates a role from the given string name.
     *
     * @param name the name of the role as a string
     * @return the corresponding {@code Role} instance
     * @throws IllegalArgumentException if the name does not match any role
     */
    public static Role toRoleFromName(String name) {
        return new Role(Roles.valueOf(name));
    }

    /**
     * Validates the provided set of roles.
     * If the set is null or empty, returns a set containing the default role.
     *
     * @param roles the set of roles to validate
     * @return a non-empty set of roles
     */
    public static Set<Role> validateRoles(Set<Role> roles) {
        return roles == null || roles.isEmpty() ? Set.of(getDefaultRole()) : roles;
    }

    /**
     * Returns the string representation of the role's name.
     *
     * @return the name of the role as a string
     */
    public String getStringName() {
        return this.name.name();
    }

    /**
     * Creates a role from the given string name.
     *
     * @param name the name of the role as a string
     * @return the corresponding {@code Role} instance
     * @throws IllegalArgumentException if the name does not match any role
     */
    public static Role create(String name) {
        return new Role(Roles.valueOf(name));
    }
}
