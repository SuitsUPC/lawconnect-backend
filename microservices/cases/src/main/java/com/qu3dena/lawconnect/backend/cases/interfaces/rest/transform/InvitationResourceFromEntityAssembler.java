package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Invitation;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.InvitationResource;

public class InvitationResourceFromEntityAssembler {
    public static InvitationResource toResourceFromEntity(Invitation entity) {
        return new InvitationResource(
                entity.getId(),
                entity.getLegalCase().getId(),
                entity.getLawyerId(),
                entity.getStatus()
        );
    }
}
