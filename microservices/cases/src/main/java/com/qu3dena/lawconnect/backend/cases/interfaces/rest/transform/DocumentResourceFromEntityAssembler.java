package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Document;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.DocumentResource;

public class DocumentResourceFromEntityAssembler {
    public static DocumentResource toResourceFromEntity(Document entity) {
        return new DocumentResource(
                entity.getId(),
                entity.getCaseId(),
                entity.getUploadedBy(),
                entity.getFilename(),
                entity.getFileUrl(),
                entity.getFileSize(),
                entity.getFileType(),
                entity.getUploadedAt()
        );
    }
}

