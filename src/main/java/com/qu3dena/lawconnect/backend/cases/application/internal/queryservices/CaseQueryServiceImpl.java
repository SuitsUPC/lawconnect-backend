package com.qu3dena.lawconnect.backend.cases.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Application;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Invitation;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.*;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.services.CaseQueryService;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.ApplicationRepository;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CaseRepository;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.InvitationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation of the {@code CaseQueryService} interface.
 * Handles queries related to cases using repositories for cases, invitations, and applications.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
public class CaseQueryServiceImpl implements CaseQueryService {

    private final CaseRepository caseRepository;
    private final InvitationRepository invitationRepository;
    private final ApplicationRepository applicationRepository;

    /**
     * Constructs a CaseQueryServiceImpl with the specified repositories.
     *
     * @param caseRepository        the repository for accessing case data
     * @param invitationRepository  the repository for accessing invitation data
     * @param applicationRepository the repository for accessing application data
     */
    public CaseQueryServiceImpl(
            CaseRepository caseRepository,
            InvitationRepository invitationRepository,
            ApplicationRepository applicationRepository
    ) {
        this.caseRepository = caseRepository;
        this.invitationRepository = invitationRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public List<CaseAggregate> handle(GetAllCasesQuery query) {
        return caseRepository.findAll();
    }

    /**
     * Handles the GetCaseByIdQuery to retrieve a case by its identifier.
     *
     * @param query the query object containing the case identifier
     * @return an Optional containing the {@code CaseAggregate} if found, or empty otherwise
     */
    @Override
    public Optional<CaseAggregate> handle(GetCaseByIdQuery query) {
        return caseRepository.findById(query.caseId());
    }

    /**
     * Handles the GetSuggestedCasesQuery to retrieve suggested cases for a lawyer.
     *
     * <p>This method retrieves all open cases and then excludes cases to which the lawyer
     * has already been invited or applied for.</p>
     *
     * @param query the query object containing the lawyer identifier
     * @return a list of suggested {@code CaseAggregate} for the lawyer
     */
    @Override
    public List<CaseAggregate> handle(GetSuggestedCasesQuery query) {
        // 1. Retrieve all cases in the OPEN state.
        var openCases = caseRepository.findByCurrentStatus(CaseStatus.OPEN);

        // 2. Get IDs of cases where the lawyer has already been invited.
        var casesWhereHeWasInvited = invitationRepository
                .findByLawyerId(query.lawyerId())
                .stream()
                .map(Invitation::getLegalCase)
                .map(CaseAggregate::getId)
                .collect(Collectors.toSet());

        // 3. Get IDs of cases where the lawyer has already applied.
        var casesWhereHeApplied = applicationRepository
                .findByLawyerId(query.lawyerId())
                .stream()
                .map(Application::getLegalCase)
                .map(CaseAggregate::getId)
                .collect(Collectors.toSet());

        // 4. Filter out cases where the lawyer was already invited or applied.
        return openCases.stream()
                .filter(case_ -> !casesWhereHeWasInvited.contains(case_.getId()))
                .filter(case_ -> !casesWhereHeApplied.contains(case_.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Handles the GetCasesByClientIdQuery to retrieve cases for a specific client.
     *
     * @param query the query object containing the client identifier
     * @return a list of {@code CaseAggregate} associated with the client
     */
    @Override
    public List<CaseAggregate> handle(GetCasesByClientIdQuery query) {
        return caseRepository.findByClientId(query.clientId());
    }

    /**
     * Handles the GetCasesByStatusQuery to retrieve cases by their current status.
     *
     * @param query the query object containing the case status
     * @return a list of {@code CaseAggregate} with the specified status
     */
    @Override
    public List<CaseAggregate> handle(GetCasesByStatusQuery query) {
        return caseRepository.findByCurrentStatus(query.status());
    }

    /**
     * Handles the GetCasesByLawyerIdQuery to retrieve cases accepted by a specific lawyer.
     *
     * @param query the query object containing the lawyer identifier
     * @return a list of {@code CaseAggregate} accepted by the lawyer
     */
    @Override
    public List<CaseAggregate> handle(GetCasesByLawyerIdQuery query) {
        return caseRepository.findByAssignedLawyerIdAndCurrentStatus(query.lawyerId(), CaseStatus.ACCEPTED);
    }
}
