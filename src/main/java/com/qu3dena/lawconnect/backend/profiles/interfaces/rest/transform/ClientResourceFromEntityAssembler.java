package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.ClientAggregate;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources.ClientResource;

public class ClientResourceFromEntityAssembler {

    public static ClientResource toResourceFromEntity(ClientAggregate entity) {

        return new ClientResource(
                entity.getId(),
                entity.getUserId(),
                entity.getFullName(),
                entity.getDniValue(),
                entity.getContact()
        );
    }
}
