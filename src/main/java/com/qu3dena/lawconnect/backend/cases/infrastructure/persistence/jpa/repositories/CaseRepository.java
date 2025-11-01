package com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing {@link CaseAggregate} entities.
 * Extends {@link JpaRepository} to provide CRUD operations and custom queries.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Repository
public interface CaseRepository extends JpaRepository<CaseAggregate, UUID> {

    /**
     * Finds all cases associated with a specific client.
     *
     * @param clientId the unique identifier of the client
     * @return a list of cases linked to the given client
     */
    List<CaseAggregate> findByClientId(UUID clientId);

    /**
     * Finds all cases with a specific status.
     *
     * @param status the current status of the cases
     * @return a list of cases with the given status
     */
    List<CaseAggregate> findByCurrentStatus(CaseStatus status);

    /**
     * Finds all cases assigned to a specific lawyer and with a specific status.
     *
     * @param lawyerId the unique identifier of the lawyer
     * @param status   the current status of the cases
     * @return a list of cases assigned to the given lawyer and with the given status
     */
    List<CaseAggregate> findByAssignedLawyerIdAndCurrentStatus(UUID lawyerId, CaseStatus status);
}
