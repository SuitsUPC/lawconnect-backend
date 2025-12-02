package com.qu3dena.lawconnect.backend.profiles.domain.model.commands;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;

import java.util.Set;
import java.util.UUID;

/**
 * Command object for updating an existing lawyer profile.
 *
 * @param userId      the unique identifier of the user associated with the profile
 * @param firstname   the updated first name of the lawyer
 * @param lastname    the updated last name of the lawyer
 * @param dni         the updated DNI of the lawyer
 * @param contactInfo the updated contact information
 * @param description the updated professional description
 * @param specialties the updated set of specialty names
 */
public record UpdateLawyerProfileCommand(
        UUID userId,
        String firstname,
        String lastname,
        String dni,
        ContactInfo contactInfo,
        String description,
        Set<String> specialties
) {
}

