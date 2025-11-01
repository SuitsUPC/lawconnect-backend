package com.qu3dena.lawconnect.backend.cases.domain.model.events;

import java.util.UUID;

/**
 * Event representing the submission of an application.
 *
 * @param caseId the unique identifier of the case associated with the submitted application
 * @param applicationId the unique identifier of the submitted application
 * @param lawyerId the unique identifier of the lawyer who submitted the application
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record ApplicationSubmittedEvent(
        UUID caseId,
        Long applicationId,
        UUID lawyerId
) {
}
