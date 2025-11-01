package com.qu3dena.lawconnect.backend.profiles.domain.model.commands;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;

import java.util.UUID;

/**
 * Command object for creating a new client.
 * <p>
 * Encapsulates the data required to create a client, including user ID, personal information,
 * national identification number (DNI), and contact information.
 *
 * @param userId      the unique identifier of the user
 * @param firstname   the first name of the client
 * @param lastname    the last name of the client
 * @param dni         the national identification number of the client
 * @param contactInfo the contact information of the client
 * @author LawConnect Team
 * @since 1.0
 */
public record CreateClientCommand(
        UUID userId,
        String firstname,
        String lastname,
        String dni,
        ContactInfo contactInfo
) {
}
