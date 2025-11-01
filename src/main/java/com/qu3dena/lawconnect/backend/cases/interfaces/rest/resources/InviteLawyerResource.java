package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import java.util.UUID;

/**
 * Resource representation for inviting a lawyer to a case.
 * Encapsulates the details required to invite a lawyer, including the case ID,
 * lawyer ID, and client ID.
 *
 * @param caseId   the unique identifier of the case associated with the invitation
 * @param lawyerId the unique identifier of the lawyer being invited
 * @param clientId the unique identifier of the client initiating the invitation
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record InviteLawyerResource(
        UUID caseId,
        UUID lawyerId,
        UUID clientId
) { }
