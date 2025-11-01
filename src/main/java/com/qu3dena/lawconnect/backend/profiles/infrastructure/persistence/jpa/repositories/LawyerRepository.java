package com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa.repositories;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.LawyerAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.LawyerSpecialties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing lawyer entities.
 * <p>
 * This repository provides methods for performing CRUD operations
 * and custom queries on {@link LawyerAggregate} entities.
 * It includes functionality for checking existence and retrieving
 * lawyers by their DNI or specialties.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Repository
public interface LawyerRepository extends JpaRepository<LawyerAggregate, UUID> {

    /**
     * Checks if a lawyer exists by their DNI value.
     *
     * @param dni the DNI value to check
     * @return true if a lawyer with the given DNI exists, false otherwise
     */
    boolean existsByDni_Value(String dni);

    /**
     * Finds a lawyer by their specialties.
     *
     * @param name the specialties of the lawyer to find
     * @return an {@link Optional} containing the found {@link LawyerAggregate}, or empty if no lawyer is found
     */
    Optional<LawyerAggregate> findBySpecialties_Name(LawyerSpecialties name);

    Optional<LawyerAggregate> findByUserId(UUID userId);
}
