package com.qu3dena.lawconnect.backend.profiles.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.LawyerAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetAllLawyersQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetLawyerByUserIdQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetLawyerBySpecialtyQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.LawyerSpecialties;
import com.qu3dena.lawconnect.backend.profiles.domain.services.LawyerQueryService;
import com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa.repositories.LawyerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link LawyerQueryService} interface.
 * <p>
 * This service handles queries related to lawyer operations,
 * such as retrieving a lawyer by their specialty or DNI.
 * It interacts with the {@link LawyerRepository} to fetch data.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
public class LawyerQueryServiceImpl implements LawyerQueryService {

    private final LawyerRepository lawyerRepository;

    /**
     * Constructs a new instance of {@code LawyerQueryServiceImpl}.
     *
     * @param lawyerRepository the repository for managing lawyer entities
     */
    public LawyerQueryServiceImpl(LawyerRepository lawyerRepository) {
        this.lawyerRepository = lawyerRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LawyerAggregate> handle(GetAllLawyersQuery query) {
        return lawyerRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LawyerAggregate> handle(GetLawyerBySpecialtyQuery query) {
        return lawyerRepository.findBySpecialties_Name(LawyerSpecialties.valueOf(query.specialty()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LawyerAggregate> handle(GetLawyerByUserIdQuery query) {
        return lawyerRepository.findByUserId(query.userId());
    }
}
