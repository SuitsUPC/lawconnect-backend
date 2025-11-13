package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import java.util.UUID;

/**
 * Resource representation for creating a legal case.
 * Encapsulates the details required to create a new case, including the client ID,
 * title, description, and specialty ID of the case.
 *
 * @param clientId    the unique identifier of the client associated with the case
 * @param title       the title of the case
 * @param description the description of the case
 * @param specialtyId the ID of the legal specialty required for this case
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record CreateCaseResource(
        UUID clientId,
        String title,
        String description,
        Long specialtyId
) { }
