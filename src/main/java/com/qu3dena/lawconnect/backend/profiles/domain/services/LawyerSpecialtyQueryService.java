package com.qu3dena.lawconnect.backend.profiles.domain.services;

import com.qu3dena.lawconnect.backend.profiles.domain.model.entities.LawyerSpecialty;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetAllLawyerSpecialtiesQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetLawyerSpecialtyByNameQuery;

import java.util.Optional;
import java.util.Set;

/**
 * Service interface for handling queries related to lawyer specialties.
 * <p>
 * Provides methods to retrieve all lawyer specialties or a specific specialty by name.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface LawyerSpecialtyQueryService {

    /**
     * Handles the query to retrieve all lawyer specialties.
     *
     * @param query the query object for retrieving all lawyer specialties
     * @return a set of all {@link LawyerSpecialty} entities
     */
    Set<LawyerSpecialty> handle(GetAllLawyerSpecialtiesQuery query);

    /**
     * Handles the query to retrieve a lawyer specialty by its name.
     *
     * @param query the query object containing the name of the specialty
     * @return an {@link Optional} containing the found {@link LawyerSpecialty}, or empty if not found
     */
    Optional<LawyerSpecialty> handle(GetLawyerSpecialtyByNameQuery query);
}
