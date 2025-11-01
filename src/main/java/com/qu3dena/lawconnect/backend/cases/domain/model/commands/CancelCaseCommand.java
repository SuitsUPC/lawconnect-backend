package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import java.util.UUID;

/**
 * Command to cancel an existing case.
 *
 * @param caseId   the ID of the case to be canceled
 * @param clientId the ID of the client associated with the case
 * @author LawConnect Team
 * @since 1.0
 */
public record CancelCaseCommand(UUID caseId, UUID clientId) {
}
