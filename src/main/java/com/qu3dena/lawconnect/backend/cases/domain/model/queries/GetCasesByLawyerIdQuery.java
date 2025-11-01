package com.qu3dena.lawconnect.backend.cases.domain.model.queries;

import java.util.UUID;

/**
 * Query to retrieve cases associated with a specific lawyer.
 * This query encapsulates the necessary data to fetch all cases linked to a given lawyer.
 *
 * @param lawyerId The unique identifier of the lawyer whose cases are to be retrieved.
 */
public record GetCasesByLawyerIdQuery(UUID lawyerId) {
}
