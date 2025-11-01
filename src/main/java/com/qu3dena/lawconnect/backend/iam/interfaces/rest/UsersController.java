package com.qu3dena.lawconnect.backend.iam.interfaces.rest;

import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetAllUsersQuery;
import com.qu3dena.lawconnect.backend.iam.domain.model.queries.GetUserByIdQuery;
import com.qu3dena.lawconnect.backend.iam.domain.services.UserQueryService;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources.UserResource;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for handling user-related endpoints.
 * <p>
 * Provides endpoints for retrieving users available in the system.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Available User Endpoints")
public class UsersController {

    private final UserQueryService userQueryService;

    /**
     * Constructs a {@code UsersController} with the specified {@link UserQueryService}.
     *
     * @param userQueryService the service handling user queries
     */
    public UsersController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    /**
     * Retrieves all users available in the system.
     *
     * @return a {@link ResponseEntity} containing a list of {@link UserResource} and HTTP status
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Get all the users available in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var getAllUsersQuery = new GetAllUsersQuery();
        var users = userQueryService.handle(getAllUsersQuery);

        var userResources = users.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity).toList();

        return ResponseEntity.ok(userResources);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Get a user by their unique identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "User not found."),
    })
    public ResponseEntity<UserResource> getUserById(@PathVariable String userId) {
        var user = userQueryService.handle(new GetUserByIdQuery(UUID.fromString(userId)));

        if (user.isEmpty())
            return ResponseEntity.notFound().build();

        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }
}
