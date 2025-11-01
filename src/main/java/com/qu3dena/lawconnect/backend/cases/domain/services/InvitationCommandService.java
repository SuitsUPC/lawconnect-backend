package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.AcceptInvitationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.InviteLawyerCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.RejectInvitationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Invitation;

import java.util.Optional;

/**
 * Service interface for handling invitation-related commands.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface InvitationCommandService {

    /**
     * Handles the rejection of an invitation.
     *
     * @param command the command containing the details of the invitation to be rejected
     */
    Optional<Invitation> handle(RejectInvitationCommand command);

    /**
     * Handles the acceptance of an invitation.
     *
     * @param command the command containing the details of the invitation to be accepted
     */
    Optional<Invitation> handle(AcceptInvitationCommand command);

    /**
     * Handles the invitation of a lawyer
     *
     * @param command the command containing the details of the invitation.
     */
    Optional<Invitation> handle(InviteLawyerCommand command);
}
