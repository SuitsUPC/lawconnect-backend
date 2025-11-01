package com.qu3dena.lawconnect.backend.cases.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Application;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetApplicationsByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.services.ApplicationQueryService;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation of the ApplicationQueryService interface.
 * Handles queries for retrieving applications associated with cases.
 *
 * <p>This implementation uses an ApplicationRepository to fetch applications from the persistence layer.</p>
 *
 * @see ApplicationQueryService
 * @see ApplicationRepository
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
public class ApplicationQueryServiceImpl implements ApplicationQueryService {

    private final ApplicationRepository applicationRepository;

    /**
     * Constructs an ApplicationQueryServiceImpl with the specified ApplicationRepository.
     *
     * @param applicationRepository the repository used for accessing application data
     */
    public ApplicationQueryServiceImpl(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * Handles the provided GetApplicationsByCaseQuery.
     *
     * @param query the query object containing the case identifier
     * @return a list of Application entities associated with the specified case
     */
    @Override
    public List<Application> handle(GetApplicationsByCaseIdQuery query) {
        return applicationRepository.findByLegalCase_Id(query.caseId());
    }
}
