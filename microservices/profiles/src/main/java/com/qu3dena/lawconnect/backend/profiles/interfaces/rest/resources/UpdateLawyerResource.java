package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;

import java.util.Set;

/**
 * Resource representing the payload required to update a lawyer profile.
 *
 * @param firstname   the updated first name
 * @param lastname    the updated last name
 * @param dni         the updated DNI
 * @param contactInfo the updated contact info
 * @param description the updated description
 * @param specialties the updated specialties
 */
public record UpdateLawyerResource(
        String firstname,
        String lastname,
        String dni,
        ContactInfo contactInfo,
        String description,
        Set<String> specialties
) {
}

