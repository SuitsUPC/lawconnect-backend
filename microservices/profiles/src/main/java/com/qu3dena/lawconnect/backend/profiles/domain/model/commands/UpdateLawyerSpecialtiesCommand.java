package com.qu3dena.lawconnect.backend.profiles.domain.model.commands;

import java.util.Set;
import java.util.UUID;

/**
 * Command for updating a lawyer's specialties.
 *
 * @param userId the unique identifier of the user (lawyer)
 * @param specialtyNames the set of specialty names to assign to the lawyer
 */
public record UpdateLawyerSpecialtiesCommand(
        UUID userId,
        Set<String> specialtyNames
) {
}

