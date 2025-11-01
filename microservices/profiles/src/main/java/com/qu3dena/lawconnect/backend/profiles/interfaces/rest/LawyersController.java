package com.qu3dena.lawconnect.backend.profiles.interfaces.rest;

import com.qu3dena.lawconnect.backend.profiles.domain.model.queries.GetLawyerByUserIdQuery;
import com.qu3dena.lawconnect.backend.profiles.domain.services.LawyerCommandService;
import com.qu3dena.lawconnect.backend.profiles.domain.services.LawyerQueryService;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources.CreateLawyerResource;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.resources.LawyerResource;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform.CreateLawyerCommandFromResourceAssembler;
import com.qu3dena.lawconnect.backend.profiles.interfaces.rest.transform.LawyerResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/lawyers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profile Management Endpoints")
public class LawyersController {

    private final LawyerCommandService commandService;
    private final LawyerQueryService queryService;

    public LawyersController(LawyerCommandService commandService, LawyerQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create lawyer profile", description = "Creates a new lawyer profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lawyer profile created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
    })
    public ResponseEntity<LawyerResource> createLawyerProfile(
            @RequestBody CreateLawyerResource resource
    ) {
        var command = CreateLawyerCommandFromResourceAssembler.toCommandFromResource(resource);

        var lawyer = commandService.handle(command);

        if (lawyer.isEmpty())
            return ResponseEntity.badRequest().build();

        var lawyerResource = LawyerResourceFromEntityAssembler.toResourceFromEntity(lawyer.get());

        return ResponseEntity.status(HttpStatus.CREATED).body(lawyerResource);
    }

    @GetMapping("{userId}")
    @Operation(summary = "Get lawyer profile by User ID", description = "Get lawyer profile by User ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lawyer profile found"),
            @ApiResponse(responseCode = "404", description = "Lawyer profile not found")
    })
    public ResponseEntity<LawyerResource> getLawyerProfileByUserId(
            @PathVariable String userId
    ) {
        var query = new GetLawyerByUserIdQuery(UUID.fromString(userId));
        var maybeItem = queryService.handle(query);

        if (maybeItem.isEmpty())
            return ResponseEntity.notFound().build();

        var resource = LawyerResourceFromEntityAssembler.toResourceFromEntity(maybeItem.get());
        return ResponseEntity.ok(resource);
    }
}
