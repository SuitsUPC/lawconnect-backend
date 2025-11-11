package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResource(
        UUID id,
        UUID caseId,
        UUID senderId,
        String content,
        LocalDateTime createdAt,
        Boolean isRead
) {
}

