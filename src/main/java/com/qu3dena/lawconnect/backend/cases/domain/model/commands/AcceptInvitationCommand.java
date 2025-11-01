package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import java.util.UUID;

/**
 * Command to accept an invitation to a case.
 *
 * @param invitationId the ID of the invitation being accepted
 * @param lawyerId the ID of the lawyer accepting the invitation
 * @since 1.0
 * @author LawConnect Team
 */
public record AcceptInvitationCommand(Long invitationId, UUID lawyerId) {
}
