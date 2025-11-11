package com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByCaseIdOrderByUploadedAtDesc(UUID caseId);
    void deleteByCaseId(UUID caseId);
}

