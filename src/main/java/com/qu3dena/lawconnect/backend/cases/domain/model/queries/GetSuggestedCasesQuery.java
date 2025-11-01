package com.qu3dena.lawconnect.backend.cases.domain.model.queries;

import java.util.UUID;

/**
 * Query to retrieve suggested cases for a specific lawyer.
 *
 * @param lawyerId the ID of the lawyer for whom suggested cases are being retrieved
 * @author LawConnect Team
 * @since 1.0
 */
public record GetSuggestedCasesQuery(UUID lawyerId) {
}
