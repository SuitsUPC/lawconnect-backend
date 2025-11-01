package com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;

import java.util.Set;
import java.util.UUID;

public record CreateLawyerResource(
        UUID userId,
        String firstname,
        String lastname,
        String dni,
        ContactInfo contactInfo,
        String description,
        Set<String> specialties
) {
}
