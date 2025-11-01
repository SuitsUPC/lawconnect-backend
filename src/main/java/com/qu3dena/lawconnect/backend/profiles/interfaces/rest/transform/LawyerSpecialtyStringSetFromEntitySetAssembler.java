package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.profiles.domain.model.entities.LawyerSpecialty;

import java.util.Set;
import java.util.stream.Collectors;

public class LawyerSpecialtyStringSetFromEntitySetAssembler {

    public static Set<String> toResourceSetFromEntitySet(Set<LawyerSpecialty> entity) {
        return entity.stream()
                .map(LawyerSpecialty::getStringName)
                .collect(Collectors.toSet());
    }
}
