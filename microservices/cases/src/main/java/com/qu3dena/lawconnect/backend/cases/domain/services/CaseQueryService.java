package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling case-related queries.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface CaseQueryService {

    /**
     * Handles the retrieval of all cases.
     *
     * @param query the query to retrieve all cases
     * @return a list of all case aggregates
     */
    List<CaseAggregate> handle(GetAllCasesQuery query);

    /**
     * Handles the retrieval of a case by its ID.
     *
     * @param query the query containing the ID of the case to be retrieved
     * @return an optional containing the case aggregate if found, or empty if not found
     */
    Optional<CaseAggregate> handle(GetCaseByIdQuery query);

    /**
     * Handles the retrieval of suggested cases for a specific lawyer.
     *
     * @param query the query containing the details of the lawyer for whom suggested cases are being retrieved
     * @return a list of suggested case aggregates
     */
    List<CaseAggregate> handle(GetSuggestedCasesQuery query);

    /**
     * Handles the retrieval of cases associated with a specific client.
     *
     * @param query the query containing the details of the client whose cases are being retrieved
     * @return a list of case aggregates associated with the specified client
     */
    List<CaseAggregate> handle(GetCasesByClientIdQuery query);

    /**
     * Handles the retrieval of cases by their status.
     *
     * @param query the query containing the status of the cases to be retrieved
     * @return a list of case aggregates with the specified status
     */
    List<CaseAggregate> handle(GetCasesByStatusQuery query);

    /**
     * Handles the retrieval of cases associated with a specific lawyer.
     *
     * @param query the query containing the ID of the lawyer whose cases are being retrieved
     * @return a list of case aggregates associated with the specified lawyer
     */
    List<CaseAggregate> handle(GetCasesByLawyerIdQuery query);
}
