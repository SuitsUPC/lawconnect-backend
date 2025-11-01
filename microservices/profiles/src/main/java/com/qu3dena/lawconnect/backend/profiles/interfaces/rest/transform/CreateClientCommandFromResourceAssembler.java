package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.CreateClientCommand;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources.CreateClientResource;

public class CreateClientCommandFromResourceAssembler {

    public static CreateClientCommand toCommandFromResource(CreateClientResource resource) {

        return new CreateClientCommand(
                resource.userId(),
                resource.firstname(),
                resource.lastname(),
                resource.dni(),
                resource.contactInfo()
        );
    }
}
