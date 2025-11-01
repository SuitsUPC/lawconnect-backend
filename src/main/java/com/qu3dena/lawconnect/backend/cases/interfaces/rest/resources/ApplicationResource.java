package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.ApplicationStatus;

import java.util.UUID;

/**
 * Resource representation of an application.
 * Encapsulates the details of an application, including its ID, associated case ID,
 * lawyer ID, and current status.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record ApplicationResource(
        Long id,
        UUID caseId,
        UUID lawyerId,
        ApplicationStatus status
) { }
