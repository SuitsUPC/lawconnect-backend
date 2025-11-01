package com.qu3dena.lawconnect.backend.cases.domain.model.events;

import java.util.UUID;

/**
 * Event representing the acceptance of an application.
 *
 * @param caseId        the unique identifier of the case associated with the accepted application
 * @param applicationId the unique identifier of the accepted application
 * @param clientId      the unique identifier of the client associated with the case
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record ApplicationAcceptedEvent(
        UUID caseId,
        Long applicationId,
        UUID clientId
) {
}
