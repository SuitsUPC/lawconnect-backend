package com.qu3dena.lawconnect.backend.profiles.domain.model.commands;

import com.qu3dena.lawconnect.backend.profiles.domain.model.entities.LawyerSpecialty;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;

import java.util.Set;
import java.util.UUID;

/**
 * Command object for creating a new lawyer.
 * <p>
 * Encapsulates the data required to create a lawyer, including user ID, personal information,
 * national identification number (DNI), contact information, description, and specialties.
 *
 * @param userId       the unique identifier of the user
 * @param firstname   the first name of the lawyer
 * @param lastname    the last name of the lawyer
 * @param dni         the national identification number of the lawyer
 * @param contactInfo the contact information of the lawyer
 * @param description a description of the lawyer
 * @param specialties the set of specialties associated with the lawyer
 * @author LawConnect Team
 * @since 1.0
 */
public record CreateLawyerCommand(
        UUID userId,
        String firstname,
        String lastname,
        String dni,
        ContactInfo contactInfo,
        String description,
        Set<LawyerSpecialty> specialties
) {
}
