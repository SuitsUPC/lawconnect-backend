package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.CaseMessage;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.MessageResource;

public class MessageResourceFromEntityAssembler {
    public static MessageResource toResourceFromEntity(CaseMessage entity) {
        return new MessageResource(
                entity.getId(),
                entity.getCaseId(),
                entity.getSenderId(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getIsRead()
        );
    }
}

