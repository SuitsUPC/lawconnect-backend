package com.qu3dena.lawconnect.backend.cases.domain.model.queries;

import java.util.UUID;

/**
 * Query to retrieve the timeline of a specific case.
 *
 * @param caseId the unique identifier of the case whose timeline is being requested
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record GetCaseTimelineQuery(UUID caseId) {
}
