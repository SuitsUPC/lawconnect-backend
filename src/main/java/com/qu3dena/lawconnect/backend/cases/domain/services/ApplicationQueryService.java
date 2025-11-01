package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Application;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetApplicationsByCaseIdQuery;

import java.util.List;

/**
 * Service interface for handling application-related queries.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface ApplicationQueryService {

    /**
     * Handles the retrieval of applications associated with a specific case.
     *
     * @param query the query containing the details of the case whose applications are being retrieved
     * @return a list of applications associated with the specified case
     */
    List<Application> handle(GetApplicationsByCaseIdQuery query);
}
