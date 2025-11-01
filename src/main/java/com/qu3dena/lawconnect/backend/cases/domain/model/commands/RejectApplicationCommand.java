package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import java.util.UUID;

/**
 * Command to reject an application.
 * This command encapsulates the necessary data to perform the rejection of an application.
 *
 * @param applicationId the unique identifier of the application to be rejected
 * @param clientId      the unique identifier of the client who is rejecting the application
 * @author LawConnect Team
 * @since 1.0.0
 */
public record RejectApplicationCommand(
        Long applicationId, UUID clientId
) {
}
