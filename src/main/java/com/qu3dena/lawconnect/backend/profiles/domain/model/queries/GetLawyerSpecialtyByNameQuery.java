package com.qu3dena.lawconnect.backend.profiles.domain.model.queries;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.LawyerSpecialties;

/**
 * Query object for retrieving a lawyer specialty by name.
 *
 * This query is used to request a specific lawyer specialty
 * based on the provided name.
 *
 * @param name the name of the lawyer specialty to retrieve
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record GetLawyerSpecialtyByNameQuery(LawyerSpecialties name) {
}
