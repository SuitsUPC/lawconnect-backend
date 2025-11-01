package com.qu3dena.lawconnect.backend.iam.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.iam.domain.model.entities.Role;
import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetAllRolesQuery;
import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetRoleByNameQuery;
import com.qu3dena.lawconnect.backend.iam.domain.services.RoleQueryService;
import com.qu3dena.lawconnect.backend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link RoleQueryService} for handling role-related queries.
 * <p>
 * Provides methods to retrieve roles from the repository based on different query criteria.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Service
public class RoleQueryServiceImpl implements RoleQueryService {

    private final RoleRepository roleRepository;

    /**
     * Creates a new instance of {@code RoleQueryServiceImpl} with the required role repository.
     *
     * @param roleRepository repository for role persistence operations
     */
    public RoleQueryServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Role> handle(GetAllRolesQuery query) {
        return roleRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Role> handle(GetRoleByNameQuery query) {
        return roleRepository.findByName(query.name());
    }
}
