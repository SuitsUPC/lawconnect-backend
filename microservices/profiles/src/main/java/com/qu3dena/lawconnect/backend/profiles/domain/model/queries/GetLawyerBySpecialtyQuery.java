package com.qu3dena.lawconnect.backend.profiles.domain.model.queries;

/**
 * Query object for retrieving lawyers by their specialty.
 *
 * @param specialty the name of the specialty to filter lawyers by
 * @author LawConnect Team
 * @since 1.0
 */
public record GetLawyerBySpecialtyQuery(String specialty) {
}
