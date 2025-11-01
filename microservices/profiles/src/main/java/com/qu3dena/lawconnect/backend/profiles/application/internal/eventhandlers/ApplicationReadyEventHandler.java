package com.qu3dena.lawconnect.backend.profiles.application.internal.eventhandlers;

import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.SeedLawyerSpecialtiesCommandService;
import com.qu3dena.lawconnect.backend.profiles.domain.services.LawyerSpecialtyCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * Event handler for the {@link ApplicationReadyEvent}.
 * <p>
 * This handler is triggered when the application is ready and ensures
 * that lawyer specialties are seeded into the database if needed.
 * It logs the start and end of the seeding process, including timestamps.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service(value = "ProfilesApplicationReadyEventHandler")
public class ApplicationReadyEventHandler {

    private final LawyerSpecialtyCommandService lawyerSpecialtyCommandService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);

    /**
     * Constructs a new instance of {@code ApplicationReadyEventHandler}.
     *
     * @param lawyerSpecialtyCommandService the service for handling lawyer specialty commands
     */
    public ApplicationReadyEventHandler(LawyerSpecialtyCommandService lawyerSpecialtyCommandService) {
        this.lawyerSpecialtyCommandService = lawyerSpecialtyCommandService;
    }

    /**
     * Handles the {@link ApplicationReadyEvent}.
     * <p>
     * This method verifies if lawyer specialties need to be seeded
     * and triggers the seeding process if necessary.
     *
     * @param event the application ready event
     */
    @EventListener
    public void on(ApplicationReadyEvent event) {
        var applicationName = event.getApplicationContext().getId();
        LOGGER.info("Starting to verify if lawyer specialties seeding is needed for {} at {}", applicationName, currentTimestamp());

        var command = new SeedLawyerSpecialtiesCommandService();
        lawyerSpecialtyCommandService.handle(command);

        LOGGER.info("Lawyer specialties seeding verification finished for {} at {}", applicationName, currentTimestamp());
    }

    /**
     * Returns the current timestamp.
     *
     * @return the current {@link Timestamp}
     */
    private Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
