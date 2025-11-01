package com.qu3dena.lawconnect.backend.cases.domain.model.events;

import java.util.UUID;

/**
 * Event representing the invitation of a lawyer to a case.
 *
 * @param caseId       the unique identifier of the case associated with the invitation
 * @param invitationId the unique identifier of the invitation
 * @param clientId     the unique identifier of the client who sent the invitation
 * @param lawyerId     the unique identifier of the lawyer who was invited
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record LawyerInvitedEvent(
        UUID caseId,
        Long invitationId,
        UUID clientId,
        UUID lawyerId
) {
}
