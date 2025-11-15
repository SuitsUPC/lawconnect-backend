package com.qu3dena.lawconnect.backend.cases.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.UploadDocumentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Document;
import com.qu3dena.lawconnect.backend.cases.domain.services.DocumentCommandService;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.DocumentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class DocumentCommandServiceImpl implements DocumentCommandService {

    private final DocumentRepository documentRepository;

    public DocumentCommandServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Optional<Document> handle(UploadDocumentCommand command) {
        var document = new Document(
                command.caseId(),
                command.uploadedBy(),
                command.filename(),
                command.fileUrl(),
                command.fileSize(),
                command.fileType(),
                command.fileContent()
        );

        var saved = documentRepository.save(document);
        return Optional.of(saved);
    }
}

