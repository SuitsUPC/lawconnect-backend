package com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing {@link Application} entities.
 * Extends {@link JpaRepository} to provide CRUD operations and custom queries.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Finds all applications associated with a specific legal case.
     *
     * @param caseId the unique identifier of the legal case
     * @return a list of applications linked to the given legal case
     */
    List<Application> findByLegalCase_Id(UUID caseId);

    /**
     * Finds all applications associated with a specific lawyer.
     *
     * @param lawyerId the unique identifier of the lawyer
     * @return a list of applications linked to the given lawyer
     */
    List<Application> findByLawyerId(UUID lawyerId);
}
