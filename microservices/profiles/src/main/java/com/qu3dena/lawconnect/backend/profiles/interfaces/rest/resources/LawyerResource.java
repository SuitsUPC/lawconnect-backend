package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.FullName;

import java.util.Set;
import java.util.UUID;

public record LawyerResource(
        UUID id,
        UUID userId,
        FullName fullName,
        String dni,
        ContactInfo contactInfo,
        String description,
        Set<String> specialties
) {
}
