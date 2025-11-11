package com.qu3dena.lawconnect.backend.cases.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.CaseMessage;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetMessagesByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.services.MessageQueryService;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CaseMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageQueryServiceImpl implements MessageQueryService {

    private final CaseMessageRepository messageRepository;

    public MessageQueryServiceImpl(CaseMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public List<CaseMessage> handle(GetMessagesByCaseIdQuery query) {
        return messageRepository.findByCaseIdOrderByCreatedAtAsc(query.caseId());
    }
}

