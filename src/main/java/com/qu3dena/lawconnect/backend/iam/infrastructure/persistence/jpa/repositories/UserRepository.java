package com.qu3dena.lawconnect.backend.iam.infrastructure.persistence.jpa.repositories;

import com.qu3dena.lawconnect.backend.iam.domain.model.aggregates.UserAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link UserAggregate} entities.
 * <p>
 * Provides methods to perform CRUD operations and custom queries for users in the database.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<UserAggregate, UUID> {

    /**
     * Finds a user by their username.
     *
     * @param username the username of the user to find
     * @return an {@link Optional} containing the found {@link UserAggregate}, or empty if not found
     */
    Optional<UserAggregate> findByUsername(String username);

    /**
     * Checks if a user exists by their username.
     *
     * @param username the username to check for existence
     * @return {@code true} if a user with the given username exists, {@code false} otherwise
     */
    boolean existsByUsername(String username);
}
