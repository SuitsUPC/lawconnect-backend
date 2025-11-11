package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Resource representation of a legal case.
 * Encapsulates the details of a case, including its ID, title, description,
 * client ID, assigned lawyer ID, status, and timestamps for creation and last update.
 *
 * @param id               the unique identifier of the case
 * @param title            the title of the case
 * @param description      the description of the case
 * @param clientId         the unique identifier of the client associated with the case
 * @param assignedLawyerId the unique identifier of the lawyer assigned to the case (nullable)
 * @param status           the current status of the case
 * @param createdAt        the timestamp when the case was created
 * @param updatedAt        the timestamp when the case was last updated
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record CaseResource(
        UUID id,
        String title,
        String description,
        UUID clientId,
        UUID assignedLawyerId,
        CaseStatus status,
        Instant createdAt,
        Instant updatedAt
) { }
