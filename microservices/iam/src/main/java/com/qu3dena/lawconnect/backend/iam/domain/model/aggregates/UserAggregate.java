package com.qu3dena.lawconnect.backend.iam.domain.model.aggregates;

import com.qu3dena.lawconnect.backend.iam.domain.model.entities.Role;
import com.qu3dena.lawconnect.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entity representing a user aggregate within the LawConnect system.
 * <p>
 * Maps to the {@code users} table and encapsulates user credentials and role assignment.
 * Inherits auditing fields and a UUID identifier from {@link AuditableAbstractAggregateRoot}.
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "users")
@EqualsAndHashCode(callSuper = true)
public class UserAggregate extends AuditableAbstractAggregateRoot<UserAggregate> {

    /**
     * The unique username of the user.
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * The hashed password of the user.
     */
    @Column(nullable = false)
    private String password;

    /**
     * The role assigned to the user.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * Default constructor initializing the user with the default role.
     */
    public UserAggregate() {
        this.role = Role.getDefaultRole();
    }

    /**
     * Constructs a user aggregate with the specified username, password, and role.
     *
     * @param username the user's username
     * @param password the user's password
     * @param role     the user's role
     */
    public UserAggregate(String username, String password, Role role) {
        this();
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * Factory method to create a new user aggregate.
     *
     * @param username the user's username
     * @param password the user's password
     * @param role     the user's role
     * @return a new {@code UserAggregate} instance
     */
    public static UserAggregate create(String username, String password, Role role) {
        return new UserAggregate(username, password, role);
    }

    /**
     * Returns the string representation of the user's role name.
     *
     * @return the role name as a string
     */
    public String getRoleName() {
        return role.getStringName();
    }
}
