package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import java.util.UUID;

public record SendMessageCommand(
        UUID caseId,
        UUID senderId,
        String content
) {
}

