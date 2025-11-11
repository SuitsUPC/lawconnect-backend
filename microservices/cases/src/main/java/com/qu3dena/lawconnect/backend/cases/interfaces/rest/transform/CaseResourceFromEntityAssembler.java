package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CaseResource;

public class CaseResourceFromEntityAssembler {

    public static CaseResource toResourceFromEntity(CaseAggregate entity) {
        return new CaseResource(
                entity.getId(),
                entity.getTitle().text(),
                entity.getDescription().text(),
                entity.getClientId(),
                entity.getAssignedLawyerId(),
                entity.getStatus(),
                entity.getCreatedAt().toInstant(),
                entity.getUpdatedAt().toInstant()
        );
    }
}
