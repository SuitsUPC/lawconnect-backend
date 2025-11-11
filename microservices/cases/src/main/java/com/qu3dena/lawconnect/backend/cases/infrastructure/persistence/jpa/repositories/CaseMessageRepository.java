package com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.CaseMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CaseMessageRepository extends JpaRepository<CaseMessage, UUID> {
    List<CaseMessage> findByCaseIdOrderByCreatedAtAsc(UUID caseId);
    void deleteByCaseId(UUID caseId);
}

