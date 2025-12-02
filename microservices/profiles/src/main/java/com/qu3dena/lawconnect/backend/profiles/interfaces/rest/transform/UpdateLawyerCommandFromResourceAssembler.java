package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.UpdateLawyerProfileCommand;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources.UpdateLawyerResource;

import java.util.UUID;

public class UpdateLawyerCommandFromResourceAssembler {

    public static UpdateLawyerProfileCommand toCommandFromResource(UUID userId, UpdateLawyerResource resource) {
        return new UpdateLawyerProfileCommand(
                userId,
                resource.firstname(),
                resource.lastname(),
                resource.dni(),
                resource.contactInfo(),
                resource.description(),
                resource.specialties()
        );
    }
}

