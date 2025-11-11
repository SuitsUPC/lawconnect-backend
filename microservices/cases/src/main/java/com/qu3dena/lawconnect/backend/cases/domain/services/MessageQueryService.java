package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.CaseMessage;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetMessagesByCaseIdQuery;

import java.util.List;

public interface MessageQueryService {
    List<CaseMessage> handle(GetMessagesByCaseIdQuery query);
}

