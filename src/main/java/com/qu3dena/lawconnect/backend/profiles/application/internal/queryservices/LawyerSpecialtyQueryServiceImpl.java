package com.qu3dena.lawconnect.backend.profiles.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.profiles.domain.model.entities.LawyerSpecialty;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetAllLawyerSpecialtiesQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetLawyerSpecialtyByNameQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.services.LawyerSpecialtyQueryService;
import com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa.repositories.LawyerSpecialtyRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of {@link LawyerSpecialtyQueryService} for handling lawyer specialty queries.
 * <p>
 * This service provides methods to retrieve all lawyer specialties or a specific specialty by name.
 * It interacts with the {@link LawyerSpecialtyRepository} to fetch data from the database.
 * The results are returned as either a set of specialties or an optional specialty entity.
 *
 * <p>Use this service to query lawyer specialties efficiently and ensure proper encapsulation
 * of the repository logic.</p>
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
public class LawyerSpecialtyQueryServiceImpl implements LawyerSpecialtyQueryService {

    private final LawyerSpecialtyRepository lawyerSpecialtyRepository;

    /**
     * Constructs a new instance of {@code LawyerSpecialtyQueryServiceImpl}.
     *
     * @param lawyerSpecialtyRepository the repository for managing lawyer specialties
     */
    public LawyerSpecialtyQueryServiceImpl(LawyerSpecialtyRepository lawyerSpecialtyRepository) {
        this.lawyerSpecialtyRepository = lawyerSpecialtyRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<LawyerSpecialty> handle(GetAllLawyerSpecialtiesQuery query) {
        return new HashSet<>(lawyerSpecialtyRepository.findAll());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LawyerSpecialty> handle(GetLawyerSpecialtyByNameQuery query) {
        return lawyerSpecialtyRepository.findByName(query.name());
    }
}
