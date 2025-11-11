package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources;

import java.util.Set;

/**
 * Resource for updating a lawyer's specialties.
 *
 * @param specialties the set of specialty names to update
 */
public record UpdateLawyerSpecialtiesResource(
        Set<String> specialties
) {
}

