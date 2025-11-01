package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import java.util.UUID;

/**
 * Command to invite a lawyer to a case.
 *
 * @param caseId   the ID of the case to which the lawyer is being invited
 * @param lawyerId the ID of the lawyer being invited
 * @param clientId the ID of the client associated with the case
 * @author LawConnect Team
 * @since 1.0
 */
public record InviteLawyerCommand(UUID caseId, UUID lawyerId, UUID clientId) {
}
