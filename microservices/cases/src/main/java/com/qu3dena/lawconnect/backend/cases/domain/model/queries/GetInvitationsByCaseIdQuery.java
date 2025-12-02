package com.qu3dena.lawconnect.backend.cases.domain.model.queries;

import java.util.UUID;

/**
 * Query object for retrieving invitations associated with a specific legal case.
 *
 * @param caseId the unique identifier of the legal case
 */
public record GetInvitationsByCaseIdQuery(UUID caseId) {
}

