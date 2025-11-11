package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.SendMessageCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.CaseMessage;

import java.util.Optional;

public interface MessageCommandService {
    Optional<CaseMessage> handle(SendMessageCommand command);
}

