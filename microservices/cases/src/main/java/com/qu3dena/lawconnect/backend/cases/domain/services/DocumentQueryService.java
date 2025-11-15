package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Document;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetDocumentsByCaseIdQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentQueryService {
    List<Document> handle(GetDocumentsByCaseIdQuery query);
    Optional<Document> findByCaseIdAndFileUrl(UUID caseId, String fileUrl);
}

