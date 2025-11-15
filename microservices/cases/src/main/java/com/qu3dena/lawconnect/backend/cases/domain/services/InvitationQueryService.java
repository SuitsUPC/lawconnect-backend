package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Invitation;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetInvitationsByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetInvitationsByLawyerIdQuery;

import java.util.List;

/**
 * Service interface for handling invitation-related queries.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface InvitationQueryService {

    /**
     * Handles the retrieval of invitations associated with a specific lawyer.
     *
     * @param query the query containing the details of the lawyer whose invitations are being retrieved
     * @return a list of invitations associated with the specified lawyer
     */
    List<Invitation> handle(GetInvitationsByLawyerIdQuery query);

    /**
     * Handles the retrieval of invitations associated with a specific legal case.
     *
     * @param query the query containing the details of the case whose invitations are being retrieved
     * @return a list of invitations associated with the specified legal case
     */
    List<Invitation> handle(GetInvitationsByCaseIdQuery query);
}
