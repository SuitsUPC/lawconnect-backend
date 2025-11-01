package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import java.util.UUID;

/**
 * Command to close a case.
 *
 * @param caseId   the ID of the case being closed
 * @param clientId the ID of the client associated with the case
 * @author LawConnect Team
 * @since 1.0
 */
public record CloseCaseCommand(UUID caseId, UUID clientId) {
}
