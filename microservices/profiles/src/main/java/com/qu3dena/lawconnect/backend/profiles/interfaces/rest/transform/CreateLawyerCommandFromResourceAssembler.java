package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.CreateLawyerCommand;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources.CreateLawyerResource;

public class CreateLawyerCommandFromResourceAssembler {

    public static CreateLawyerCommand toCommandFromResource(CreateLawyerResource resource) {

        var specialties = LawyerSpecialtySetFromStringAssembler.toLawyerSpecialtySetFromString(resource.specialties());

        return new CreateLawyerCommand(
                resource.userId(),
                resource.firstname(),
                resource.lastname(),
                resource.dni(),
                resource.contactInfo(),
                resource.description(),
                specialties
        );
    }
}
