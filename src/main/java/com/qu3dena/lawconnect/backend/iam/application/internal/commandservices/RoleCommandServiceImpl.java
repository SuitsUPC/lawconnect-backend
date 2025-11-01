package com.qu3dena.lawconnect.backend.iam.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.iam.domain.model.commands.SeedRolesCommand;
import com.qu3dena.lawconnect.backend.iam.domain.model.entities.Role;
import com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects.Roles;
import com.qu3dena.lawconnect.backend.iam.domain.services.RoleCommandService;
import com.qu3dena.lawconnect.backend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Implementation of {@link RoleCommandService} for handling role-related commands.
 * <p>
 * Provides functionality to seed roles into the system if they do not already exist.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Service
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    /**
     * Creates a new instance of {@code RoleCommandServiceImpl} with the required role repository.
     *
     * @param roleRepository repository for role persistence operations
     */
    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(Roles.values()).forEach(role -> {
            if (!roleRepository.existsByName(role))
                roleRepository.save(Role.create(role.name()));
        });
    }
}
