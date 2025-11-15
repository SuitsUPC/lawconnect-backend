package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;

/**
 * Resource representing the payload required to update a client profile.
 *
 * @param firstname   the updated first name
 * @param lastname    the updated last name
 * @param dni         the updated DNI
 * @param contactInfo the updated contact information
 */
public record UpdateClientResource(
        String firstname,
        String lastname,
        String dni,
        ContactInfo contactInfo
) {
}

