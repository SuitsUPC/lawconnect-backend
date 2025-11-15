package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import java.util.UUID;

public record UploadDocumentCommand(
        UUID caseId,
        UUID uploadedBy,
        String filename,
        String fileUrl,
        Long fileSize,
        String fileType,
        byte[] fileContent
) {
}

