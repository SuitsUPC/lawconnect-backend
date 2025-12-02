package com.qu3dena.lawconnect.backend.cases.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Invitation;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetInvitationsByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetInvitationsByLawyerIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.InvitationStatus;
import com.qu3dena.lawconnect.backend.cases.domain.services.InvitationQueryService;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.InvitationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation of the {@code InvitationQueryService} interface.
 * Handles queries related to retrieving invitations for a lawyer.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
public class InvitationQueryServiceImpl implements InvitationQueryService {

    private final InvitationRepository invitationRepository;

    /**
     * Constructs an {@code InvitationQueryServiceImpl} with the specified {@code InvitationRepository}.
     *
     * @param invitationRepository the repository used for accessing invitation data
     */
    public InvitationQueryServiceImpl(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    /**
     * Handles the {@code GetInvitationsByLawyerIdQuery} to retrieve invitations for a specific lawyer.
     *
     * @param query the query object containing the lawyer identifier
     * @return a list of {@code Invitation} entities for the specified lawyer
     */
    @Override
    public List<Invitation> handle(GetInvitationsByLawyerIdQuery query) {
        return invitationRepository.findByLawyerIdAndStatus(query.lawyerId(), InvitationStatus.PENDING);
    }

    /**
     * Handles the {@code GetInvitationsByCaseIdQuery} to retrieve invitations for a specific legal case.
     *
     * @param query the query object containing the case identifier
     * @return a list of {@code Invitation} entities for the specified legal case
     */
    @Override
    public List<Invitation> handle(GetInvitationsByCaseIdQuery query) {
        return invitationRepository.findByLegalCase_IdAndStatus(query.caseId(), InvitationStatus.PENDING);
    }
}
