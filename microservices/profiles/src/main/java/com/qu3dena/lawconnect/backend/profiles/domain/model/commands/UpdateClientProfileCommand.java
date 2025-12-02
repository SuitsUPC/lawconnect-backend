package com.qu3dena.lawconnect.backend.profiles.domain.model.commands;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;

import java.util.UUID;

/**
 * Command object for updating an existing client profile.
 *
 * @param userId      the unique identifier of the user associated with the profile
 * @param firstname   the updated first name of the client
 * @param lastname    the updated last name of the client
 * @param dni         the updated DNI of the client
 * @param contactInfo the updated contact information
 */
public record UpdateClientProfileCommand(
        UUID userId,
        String firstname,
        String lastname,
        String dni,
        ContactInfo contactInfo
) {
}

