package com.qu3dena.lawconnect.backend.cases.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.SendMessageCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.CaseMessage;
import com.qu3dena.lawconnect.backend.cases.domain.services.MessageCommandService;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CaseMessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class MessageCommandServiceImpl implements MessageCommandService {

    private final CaseMessageRepository messageRepository;

    public MessageCommandServiceImpl(CaseMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Optional<CaseMessage> handle(SendMessageCommand command) {
        var message = new CaseMessage(
                command.caseId(),
                command.senderId(),
                command.content()
        );

        var saved = messageRepository.save(message);
        return Optional.of(saved);
    }
}

