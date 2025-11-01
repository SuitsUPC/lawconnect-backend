package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.LawyerAggregate;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources.LawyerResource;

public class LawyerResourceFromEntityAssembler {

    public static LawyerResource toResourceFromEntity(LawyerAggregate entity) {

        return new LawyerResource(
                entity.getId(),
                entity.getUserId(),
                entity.getFullName(),
                entity.getDniValue(),
                entity.getContact(),
                entity.getDescriptionText(),
                LawyerSpecialtyStringSetFromEntitySetAssembler.toResourceSetFromEntitySet(entity.getSpecialties())
        );
    }
}
