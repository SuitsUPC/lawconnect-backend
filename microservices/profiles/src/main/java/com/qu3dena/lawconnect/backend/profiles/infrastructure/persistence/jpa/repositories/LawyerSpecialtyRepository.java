package com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa.repositories;

import com.qu3dena.lawconnect.backend.profiles.domain.model.entities.LawyerSpecialty;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.Dni;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.LawyerSpecialties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link LawyerSpecialty} entities.
 * Provides methods to perform CRUD operations and custom queries on lawyer specialties.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Repository
public interface LawyerSpecialtyRepository extends JpaRepository<LawyerSpecialty, Long> {

    /**
     * Finds a {@link LawyerSpecialty} by its name.
     *
     * @param name the name of the lawyer specialty
     * @return an {@link Optional} containing the found {@link LawyerSpecialty}, or empty if not found
     */
    Optional<LawyerSpecialty> findByName(LawyerSpecialties name);

    /**
     * Checks if a {@link LawyerSpecialty} exists by its name.
     *
     * @param name the name of the lawyer specialty
     * @return true if a specialty with the given name exists, false otherwise
     */
    boolean existsByName(LawyerSpecialties name);
}
