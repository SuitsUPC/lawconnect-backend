package com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Invitation;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link Invitation} entities.
 * Extends {@link JpaRepository} to provide CRUD operations and custom queries.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    /**
     * Finds all invitations associated with a specific lawyer and having the given status.
     *
     * @param lawyerId the unique identifier of the lawyer
     * @param status   the invitation status to filter by
     * @return a list of invitations matching the given lawyer ID and status
     */
    List<Invitation> findByLawyerIdAndStatus(UUID lawyerId, InvitationStatus status);

    /**
     * Finds all invitations associated with a specific lawyer.
     *
     * @param lawyerId the unique identifier of the lawyer
     * @return a list of invitations linked to the given lawyer
     */
    List<Invitation> findByLawyerId(UUID lawyerId);

    /**
     * Finds all invitations associated with a specific legal case.
     *
     * @param caseId the unique identifier of the legal case
     * @return a list of invitations linked to the given legal case
     */
    List<Invitation> findByLegalCase_Id(UUID caseId);

    /**
     * Finds all invitations for a specific case filtered by status.
     *
     * @param caseId the unique identifier of the legal case
     * @param status the status to filter invitations by
     * @return a list of invitations linked to the case and matching the provided status
     */
    List<Invitation> findByLegalCase_IdAndStatus(UUID caseId, InvitationStatus status);

    /**
     * Finds a specific invitation associated with a lawyer and a legal case.
     *
     * @param lawyerId the unique identifier of the lawyer
     * @param caseId   the unique identifier of the legal case
     * @return an optional containing the invitation if found, or empty if not
     */
    Optional<Invitation> findByLawyerIdAndLegalCase_Id(UUID lawyerId, UUID caseId);
}
