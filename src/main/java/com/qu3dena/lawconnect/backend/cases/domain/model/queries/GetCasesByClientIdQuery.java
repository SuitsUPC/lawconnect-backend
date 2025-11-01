package com.qu3dena.lawconnect.backend.cases.domain.model.queries;

import java.util.UUID;

/**
 * Query to retrieve cases associated with a specific client.
 *
 * @param clientId the ID of the client whose cases are being retrieved
 * @author LawConnect Team
 * @since 1.0
 */
public record GetCasesByClientIdQuery(UUID clientId) {
}
