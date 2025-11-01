package com.qu3dena.lawconnect.backend.iam.interfaces.rest;

import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetAllRolesQuery;
import com.qu3dena.lawconnect.backend.iam.domain.services.RoleQueryService;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources.RoleResource;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.transform.RoleResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for handling role-related endpoints.
 * <p>
 * Provides endpoints for retrieving available roles in the system.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@RestController
@RequestMapping(value = "/api/v1/roles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Roles", description = "Available Role Endpoints")
public class RolesController {
    private final RoleQueryService roleQueryService;

    /**
     * Constructs a {@code RolesController} with the specified {@link RoleQueryService}.
     *
     * @param roleQueryService the service handling role queries
     */
    public RolesController(RoleQueryService roleQueryService) {
        this.roleQueryService = roleQueryService;
    }

    /**
     * Retrieves all roles available in the system.
     *
     * @return a {@link ResponseEntity} containing a set of {@link RoleResource} and HTTP status
     */
    @GetMapping
    @Operation(summary = "Get all roles", description = "Get all roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully.")})
    public ResponseEntity<Set<RoleResource>> getAllRoles() {
        var getAllRolesQuery = new GetAllRolesQuery();
        var roles = roleQueryService.handle(getAllRolesQuery);

        var roleResources = roles.stream()
                .map(RoleResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(roleResources);
    }
}
