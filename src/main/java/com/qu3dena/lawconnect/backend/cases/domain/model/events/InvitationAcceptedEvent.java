package com.qu3dena.lawconnect.backend.cases.domain.model.events;

import java.util.UUID;

/**
 * Event representing the acceptance of an invitation.
 *
 * @param caseId the unique identifier of the case associated with the invitation
 * @param invitationId the unique identifier of the accepted invitation
 * @param lawyerId the unique identifier of the lawyer who accepted the invitation
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record InvitationAcceptedEvent(
        UUID caseId,
        Long invitationId,
        UUID lawyerId
) {
}
