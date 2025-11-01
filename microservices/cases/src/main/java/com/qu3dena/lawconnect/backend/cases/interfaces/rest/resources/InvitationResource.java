package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.InvitationStatus;

import java.util.UUID;

/**
 * Resource representation for an invitation.
 * Encapsulates the details of an invitation, including the invitation ID,
 * associated case ID, lawyer ID, and the status of the invitation.
 *
 * @param id       the unique identifier of the invitation
 * @param caseId   the unique identifier of the case associated with the invitation
 * @param lawyerId the unique identifier of the lawyer associated with the invitation
 * @param status   the status of the invitation (e.g., PENDING, ACCEPTED, REJECTED)
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record InvitationResource(
        Long id,
        UUID caseId,
        UUID lawyerId,
        InvitationStatus status
) { }
