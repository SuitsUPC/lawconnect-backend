package com.qu3dena.lawconnect.backend.cases.domain.model.queries;

import java.util.UUID;

/**
 * Query to retrieve comments associated with a specific case.
 *
 * @param caseId the ID of the case for which comments are being retrieved
 * @author LawConnect Team
 * @since 1.0
 */
public record GetCommentsByCaseIdQuery(UUID caseId) {
}
