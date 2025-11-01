package com.qu3dena.lawconnect.backend.cases.domain.model.events;

import java.util.UUID;

/**
 * Event representing the closure of a case.
 *
 * @param caseId the unique identifier of the closed case
 * @param clientId the unique identifier of the client associated with the case
 * @author LawConnect Team
 * @since 1.0
 */
public record CaseClosedEvent(
        UUID caseId,
        UUID clientId
) {
}
