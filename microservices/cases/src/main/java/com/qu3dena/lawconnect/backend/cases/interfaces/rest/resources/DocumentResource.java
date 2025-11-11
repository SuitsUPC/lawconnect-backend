package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResource(
        UUID id,
        UUID caseId,
        UUID uploadedBy,
        String filename,
        String fileUrl,
        Long fileSize,
        String fileType,
        LocalDateTime uploadedAt
) {
}

