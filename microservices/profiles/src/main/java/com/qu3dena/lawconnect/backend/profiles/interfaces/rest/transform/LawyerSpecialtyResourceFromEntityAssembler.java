package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.profiles.domain.model.entities.LawyerSpecialty;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources.LawyerSpecialtyResource;

public class LawyerSpecialtyResourceFromEntityAssembler {

    public static LawyerSpecialtyResource toResourceFromEntity(LawyerSpecialty entity) {
        return new LawyerSpecialtyResource(
                entity.getId(),
                entity.getStringName()
        );
    }
}
