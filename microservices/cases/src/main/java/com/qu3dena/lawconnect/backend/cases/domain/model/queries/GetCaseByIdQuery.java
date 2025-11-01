package com.qu3dena.lawconnect.backend.cases.domain.model.queries;

import java.util.UUID;

/**
 * Query to retrieve a case by its ID.
 *
 * @param caseId the ID of the case to be retrieved
 * @author LawConnect Team
 * @since 1.0
 */
public record GetCaseByIdQuery(UUID caseId) {
}
