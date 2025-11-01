package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import java.util.UUID;

/**
 * Command to accept an application for a case.
 *
 * @param applicationId the ID of the application being accepted
 * @param clientId the ID of the client associated with the application
 * @since 1.0
 * @author LawConnect Team
 */
public record AcceptApplicationCommand(Long applicationId, UUID clientId) {
}
