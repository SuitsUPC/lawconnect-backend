package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.UploadDocumentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Document;

import java.util.Optional;

public interface DocumentCommandService {
    Optional<Document> handle(UploadDocumentCommand command);
}

