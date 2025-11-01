package com.qu3dena.lawconnect.backend.cases.domain.model.queries;

import java.util.UUID;

/**
 * Query to retrieve invitations associated with a specific lawyer.
 *
 * @param lawyerId the ID of the lawyer whose invitations are being retrieved
 * @author LawConnect Team
 * @since 1.0
 */
public record GetInvitationsByLawyerIdQuery(UUID lawyerId) {
}
