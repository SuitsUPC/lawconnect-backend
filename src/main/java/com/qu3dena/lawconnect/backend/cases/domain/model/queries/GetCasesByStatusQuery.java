package com.qu3dena.lawconnect.backend.cases.domain.model.queries;

import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;

/**
 * Query to retrieve cases based on their status.
 *
 * @param status the status of the cases to be retrieved
 * @author LawConnect Team
 * @since 1.0
 */
public record GetCasesByStatusQuery(CaseStatus status) {
}
