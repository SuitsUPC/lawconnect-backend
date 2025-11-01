package com.qu3dena.lawconnect.backend.profiles.domain.services;

import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.SeedLawyerSpecialtiesCommandService;

/**
 * Service interface for handling commands related to lawyer specialties.
 * <p>
 * This service defines the contract for processing commands
 * related to lawyer specialty operations, such as seeding specialties.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface LawyerSpecialtyCommandService {

    /**
     * Handles the given command to seed lawyer specialties.
     *
     * @param command the command containing the seeding logic or data
     */
    void handle(SeedLawyerSpecialtiesCommandService command);
}
