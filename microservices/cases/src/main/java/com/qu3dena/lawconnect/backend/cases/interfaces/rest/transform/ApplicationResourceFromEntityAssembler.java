package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Application;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.ApplicationResource;

public class ApplicationResourceFromEntityAssembler {

    public static ApplicationResource toResourceFromEntity(Application entity) {
        return new ApplicationResource(
                entity.getId(),
                entity.getLegalCase().getId(),
                entity.getLawyerId(),
                entity.getMessage(),
                entity.getStatus()
        );
    }
}
