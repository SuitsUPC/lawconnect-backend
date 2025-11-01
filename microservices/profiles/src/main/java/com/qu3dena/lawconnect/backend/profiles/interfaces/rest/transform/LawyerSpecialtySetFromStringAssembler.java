package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.profiles.domain.model.entities.LawyerSpecialty;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class LawyerSpecialtySetFromStringAssembler {

    public static Set<LawyerSpecialty> toLawyerSpecialtySetFromString(Set<String> setResource) {
        return Objects.nonNull(setResource)
                ? setResource.stream()
                .map(LawyerSpecialty::toLawyerSpecialtyFromName)
                .collect(Collectors.toSet()) : Collections.emptySet();
    }
}
