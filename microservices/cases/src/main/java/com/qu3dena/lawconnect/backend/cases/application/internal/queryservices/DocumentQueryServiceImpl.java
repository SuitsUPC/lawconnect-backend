package com.qu3dena.lawconnect.backend.cases.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Document;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetDocumentsByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.services.DocumentQueryService;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentQueryServiceImpl implements DocumentQueryService {

    private final DocumentRepository documentRepository;

    public DocumentQueryServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public List<Document> handle(GetDocumentsByCaseIdQuery query) {
        return documentRepository.findByCaseIdOrderByUploadedAtDesc(query.caseId());
    }

    @Override
    public Optional<Document> findByCaseIdAndFileUrl(UUID caseId, String fileUrl) {
        return documentRepository.findByCaseIdAndFileUrl(caseId, fileUrl);
    }
}

