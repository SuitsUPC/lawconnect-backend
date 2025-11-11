package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.UpdateLawyerSpecialtiesCommand;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources.UpdateLawyerSpecialtiesResource;

import java.util.UUID;

/**
 * Assembler for converting UpdateLawyerSpecialtiesResource to UpdateLawyerSpecialtiesCommand.
 */
public class UpdateLawyerSpecialtiesCommandFromResourceAssembler {

    /**
     * Converts a resource and userId to a command.
     *
     * @param userId the user ID
     * @param resource the resource containing specialty names
     * @return the update command
     */
    public static UpdateLawyerSpecialtiesCommand toCommandFromResource(UUID userId, UpdateLawyerSpecialtiesResource resource) {
        return new UpdateLawyerSpecialtiesCommand(userId, resource.specialties());
    }
}

