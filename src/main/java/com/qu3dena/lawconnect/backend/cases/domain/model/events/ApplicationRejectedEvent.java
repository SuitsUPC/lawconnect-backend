package com.qu3dena.lawconnect.backend.cases.domain.model.events;

import java.util.UUID;

/**
 * Event representing the rejection of an application.
 *
 * @param caseId        the unique identifier of the case associated with the rejected application
 * @param applicationId the unique identifier of the rejected application
 * @param clientId      the unique identifier of the client associated with the case
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record ApplicationRejectedEvent(
        UUID caseId,
        Long applicationId,
        UUID clientId
) {
}
