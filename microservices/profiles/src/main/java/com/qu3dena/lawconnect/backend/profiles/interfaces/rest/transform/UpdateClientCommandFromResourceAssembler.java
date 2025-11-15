package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.UpdateClientProfileCommand;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources.UpdateClientResource;

import java.util.UUID;

public class UpdateClientCommandFromResourceAssembler {

    public static UpdateClientProfileCommand toCommandFromResource(UUID userId, UpdateClientResource resource) {
        return new UpdateClientProfileCommand(
                userId,
                resource.firstname(),
                resource.lastname(),
                resource.dni(),
                resource.contactInfo()
        );
    }
}

