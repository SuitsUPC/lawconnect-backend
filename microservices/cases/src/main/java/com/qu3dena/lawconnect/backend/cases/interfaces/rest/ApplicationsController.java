package com.qu3dena.lawconnect.backend.cases.interfaces.rest;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.AcceptApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.RejectApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetApplicationsByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.services.ApplicationCommandService;
import com.qu3dena.lawconnect.backend.cases.domain.services.ApplicationQueryService;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.ApplicationResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.SubmitApplicationResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.ApplicationResourceFromEntityAssembler;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.SubmitApplicationCommandFromResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * REST controller for managing applications.
 * Provides endpoints for submitting, accepting, rejecting, and retrieving applications related to cases.
 *
 * <p>This controller uses {@code ApplicationCommandService} and {@code ApplicationQueryService}
 * to handle application operations.</p>
 *
 * @see ApplicationCommandService
 * @see ApplicationQueryService
 *
 * @author LawConnect Team
 * @since 1.0
 */
@RestController
@RequestMapping(value = "/api/v1/applications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Applications", description = "Applications Management Endpoints")
public class ApplicationsController {

    private final ApplicationCommandService applicationCommandService;
    private final ApplicationQueryService applicationQueryService;

    /**
     * Constructs an ApplicationsController with the specified command and query services.
     *
     * @param applicationCommandService the service for handling application commands
     * @param applicationQueryService   the service for handling application queries
     */
    public ApplicationsController(
            ApplicationCommandService applicationCommandService,
            ApplicationQueryService applicationQueryService
    ) {
        this.applicationCommandService = applicationCommandService;
        this.applicationQueryService = applicationQueryService;
    }

    /**
     * Submits a new application for a case.
     *
     * @param resource the resource containing application submission details
     * @return a ResponseEntity containing the created ApplicationResource and HTTP status 201
     * @throws IllegalStateException if the application submission fails
     */
    @PostMapping
    @Operation(summary = "Submit a new application", description = "Creates a new application for a case.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<ApplicationResource> submitApplication(
            @RequestBody SubmitApplicationResource resource
    ) {
        var command = SubmitApplicationCommandFromResourceAssembler.toCommandFromResource(resource);

        var saved = applicationCommandService.handle(command)
                .map(ApplicationResourceFromEntityAssembler::toResourceFromEntity)
                .orElseThrow(() -> new IllegalStateException("Failed to submit application"));

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Accepts an application for a case.
     *
     * @param applicationId the unique identifier of the application to be accepted
     * @param clientId      the unique identifier of the client performing this action
     * @return a ResponseEntity containing the updated ApplicationResource and HTTP status 200
     * @throws IllegalStateException if the application acceptance fails
     */
    @PutMapping("/{applicationId}/accept")
    @Operation(summary = "Accept an application", description = "Accepts an application for a case.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application accepted successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<ApplicationResource> acceptApplication(
            @PathVariable("applicationId") Long applicationId,
            @RequestParam UUID clientId
    ) {
        var command = new AcceptApplicationCommand(applicationId, clientId);

        var updated = applicationCommandService.handle(command)
                .map(ApplicationResourceFromEntityAssembler::toResourceFromEntity)
                .orElseThrow(() -> new IllegalStateException("Failed to accept application"));

        return ResponseEntity.ok(updated);
    }

    /**
     * Rejects an application for a case.
     *
     * @param applicationId the unique identifier of the application to be rejected
     * @param clientId      the unique identifier of the client performing this action
     * @return a ResponseEntity containing the updated ApplicationResource and HTTP status 200
     * @throws IllegalStateException if the application rejection fails
     */
    @PutMapping("/{applicationId}/reject")
    @Operation(summary = "Reject an application", description = "Rejects an application for a case.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application rejected successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<ApplicationResource> rejectApplication(
            @PathVariable("applicationId") Long applicationId,
            @RequestParam UUID clientId
    ) {
        var command = new RejectApplicationCommand(applicationId, clientId);

        var updated = applicationCommandService.handle(command)
                .map(ApplicationResourceFromEntityAssembler::toResourceFromEntity)
                .orElseThrow(() -> new IllegalStateException("Failed to reject application"));

        return ResponseEntity.ok(updated);
    }

    /**
     * Retrieves all applications for a specific case.
     *
     * @param caseId the unique identifier of the case for which applications are retrieved
     * @return a ResponseEntity containing the list of ApplicationResource and HTTP status 200
     */
    @GetMapping
    @Operation(summary = "Get applications by case", description = "Retrieves all applications for a specific case.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found", content = @Content)
    })
    public ResponseEntity<List<ApplicationResource>> getApplicationsByCase(@RequestParam UUID caseId) {
        var list = applicationQueryService.handle(
                        new GetApplicationsByCaseIdQuery(caseId)
                ).stream().map(ApplicationResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }
}
