package com.qu3dena.lawconnect.backend.profiles.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.LawyerAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.commands.CreateLawyerCommand;
import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.Dni;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.FullName;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.LawyerSpecialties;
import com.qu3dena.lawconnect.backend.profiles.domain.services.LawyerCommandService;
import com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa.repositories.LawyerRepository;
import com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa.repositories.LawyerSpecialtyRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link LawyerCommandService} interface.
 * <p>
 * This service handles commands related to lawyer operations,
 * such as creating a new lawyer.
 * It ensures that the lawyer's specialties exist and validates
 * the uniqueness of the lawyer's DNI.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
@Transactional
public class LawyerCommandServiceImpl implements LawyerCommandService {

    private final LawyerRepository lawyerRepository;
    private final LawyerSpecialtyRepository lawyerSpecialtyRepository;

    /**
     * Constructs a new instance of {@code LawyerCommandServiceImpl}.
     *
     * @param lawyerRepository          the repository for managing lawyer entities
     * @param lawyerSpecialtyRepository the repository for managing lawyer specialties
     */
    public LawyerCommandServiceImpl(LawyerRepository lawyerRepository, LawyerSpecialtyRepository lawyerSpecialtyRepository) {
        this.lawyerRepository = lawyerRepository;
        this.lawyerSpecialtyRepository = lawyerSpecialtyRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LawyerAggregate> handle(CreateLawyerCommand command) {

        if (lawyerRepository.existsByDni_Value(command.dni()))
            throw new IllegalArgumentException("Lawyer with DNI " + command.dni() + " already exists.");

        var specialties = command.specialties().stream()
                .map(specialty -> lawyerSpecialtyRepository
                        .findByName(LawyerSpecialties.valueOf(specialty.getStringName()))
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Specialty not found: " + specialty.getStringName())))
                .collect(Collectors.toSet());

        var lawyer = LawyerAggregate.create(
                command.userId(),
                new FullName(command.firstname(), command.lastname()),
                command.contactInfo(),
                new Dni(command.dni()),
                new Description(command.description()),
                specialties
        );

        var saved = lawyerRepository.save(lawyer);

        return Optional.of(saved);
    }
}
