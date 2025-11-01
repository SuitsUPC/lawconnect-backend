package com.qu3dena.lawconnect.backend.cases.domain.model.events;

import java.util.UUID;

/**
 * Event representing the rejection of an invitation.
 *
 * @param caseId the unique identifier of the case associated with the invitation
 * @param invitationId the unique identifier of the rejected invitation
 * @param lawyerId the unique identifier of the lawyer who rejected the invitation
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record InvitationRejectedEvent(
        UUID caseId,
        Long invitationId,
        UUID lawyerId
) {
}
