package com.qu3dena.lawconnect.backend.profiles.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.SeedLawyerSpecialtiesCommandService;
import com.qu3dena.lawconnect.backend.profiles.domain.model.entities.LawyerSpecialty;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.LawyerSpecialties;
import com.qu3dena.lawconnect.backend.profiles.domain.services.LawyerSpecialtyCommandService;
import com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa.repositories.LawyerSpecialtyRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Implementation of the {@link LawyerSpecialtyCommandService} interface.
 * <p>
 * This service handles commands related to lawyer specialties,
 * such as seeding predefined specialties into the database.
 * It ensures that specialties are only added if they do not already exist.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
public class LawyerSpecialtyCommandServiceImpl implements LawyerSpecialtyCommandService {

    private final LawyerSpecialtyRepository lawyerSpecialtyRepository;

    /**
     * Constructs a new instance of {@code LawyerSpecialtyCommandServiceImpl}.
     *
     * @param lawyerSpecialtyRepository the repository for managing lawyer specialties
     */
    public LawyerSpecialtyCommandServiceImpl(LawyerSpecialtyRepository lawyerSpecialtyRepository) {
        this.lawyerSpecialtyRepository = lawyerSpecialtyRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(SeedLawyerSpecialtiesCommandService command) {
        Arrays.stream(LawyerSpecialties.values()).forEach(specialty -> {
            if (!lawyerSpecialtyRepository.existsByName(specialty))
                lawyerSpecialtyRepository.save(LawyerSpecialty.create(specialty.name()));
        });
    }
}
