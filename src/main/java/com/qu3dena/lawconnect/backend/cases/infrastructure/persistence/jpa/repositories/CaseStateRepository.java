package com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.CaseState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing {@link CaseState} entities.
 * Extends {@link JpaRepository} to provide CRUD operations and custom queries.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Repository
public interface CaseStateRepository extends JpaRepository<CaseState, Long> {

    /**
     * Finds all case states associated with a specific legal case, ordered by the timestamp in ascending order.
     *
     * @param caseId the unique identifier of the legal case
     * @return a list of case states linked to the given legal case, ordered by the timestamp
     */
    List<CaseState> findByLegalCase_IdOrderByAtAsc(UUID caseId);
}
