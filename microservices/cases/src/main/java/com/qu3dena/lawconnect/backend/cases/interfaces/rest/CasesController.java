package com.qu3dena.lawconnect.backend.cases.interfaces.rest;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CancelCaseCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CloseCaseCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.*;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.services.CaseCommandService;
import com.qu3dena.lawconnect.backend.cases.domain.services.CaseQueryService;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CaseResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateCaseResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.CaseResourceFromEntityAssembler;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.CreateCaseCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * REST controller for managing cases.
 * Provides endpoints for creating, closing, canceling, and retrieving cases.
 *
 * <p>The controller uses {@code CaseCommandService} and {@code CaseQueryService} for handling
 * case-related operations.</p>
 *
 * @author LawConnect Team
 * @see CaseCommandService
 * @see CaseQueryService
 * @since 1.0
 */
@RestController
@RequestMapping(value = "/api/v1/cases", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Cases", description = "Cases Management Endpoints")
public class CasesController {

    private final CaseCommandService caseCommandService;
    private final CaseQueryService caseQueryService;

    /**
     * Constructs a CasesController with the specified command and query services.
     *
     * @param caseCommandService the service to handle case commands
     * @param caseQueryService   the service to handle case queries
     */
    public CasesController(CaseCommandService caseCommandService, CaseQueryService caseQueryService) {
        this.caseCommandService = caseCommandService;
        this.caseQueryService = caseQueryService;
    }

    /**
     * Creates a new case.
     *
     * @param resource the resource containing case creation details
     * @return a ResponseEntity with the created CaseResource and HTTP status 201
     * @throws IllegalStateException if the case creation fails
     */
    @PostMapping
    @Operation(summary = "Create a new case", description = "Creates a new case for a client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Case created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<CaseResource> createCase(@RequestBody CreateCaseResource resource) {
        var command = CreateCaseCommandFromResourceAssembler.toCommandFromResource(resource);

        var created = caseCommandService.handle(command)
                .map(CaseResourceFromEntityAssembler::toResourceFromEntity)
                .orElseThrow(() -> new IllegalStateException("Failed to create case"));

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Closes an existing case.
     *
     * @param caseId   the unique identifier of the case to close
     * @param clientId the unique identifier of the client closing the case
     * @return a ResponseEntity with the updated CaseResource and HTTP status 200
     * @throws IllegalStateException if the case closing fails
     */
    @PutMapping("/{caseId}/close")
    @Operation(summary = "Close a case", description = "Closes an existing case for a client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case closed successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found")
    })
    public ResponseEntity<CaseResource> closeCase(@PathVariable("caseId") UUID caseId, @RequestParam UUID clientId) {

        try {
            var command = new CloseCaseCommand(caseId, clientId);

            var maybeClosed = caseCommandService.handle(command);

            if (maybeClosed.isEmpty())
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

            var closed = maybeClosed
                    .map(CaseResourceFromEntityAssembler::toResourceFromEntity)
                    .get();

            return ResponseEntity.ok(closed);

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Cancels an existing case.
     *
     * @param caseId   the unique identifier of the case to cancel
     * @param clientId the unique identifier of the client canceling the case
     * @return a ResponseEntity with the updated CaseResource and HTTP status 200
     * @throws IllegalStateException if the case cancellation fails
     */
    @PutMapping("/{caseId}/cancel")
    @Operation(summary = "Cancel a case", description = "Cancels an existing case for a client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case canceled successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found")
    })
    public ResponseEntity<CaseResource> cancelCase(@PathVariable("caseId") UUID caseId, @RequestParam UUID clientId) {
        var command = new CancelCaseCommand(caseId, clientId);

        var canceled = caseCommandService.handle(command)
                .map(CaseResourceFromEntityAssembler::toResourceFromEntity)
                .orElseThrow(() -> new IllegalStateException("Failed to cancel case"));

        return ResponseEntity.ok(canceled);
    }

    @GetMapping
    @Operation(summary = "Get all cases", description = "Retrieves all cases.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cases retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No cases found")
    })
    public ResponseEntity<List<CaseResource>> getAllCases() {
        var list = caseQueryService.handle(new GetAllCasesQuery())
                .stream().map(CaseResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves a case by its identifier.
     *
     * @param caseId the unique identifier of the case
     * @return a ResponseEntity with the retrieved CaseResource and HTTP status 200
     * @throws IllegalStateException if the case is not found
     */
    @GetMapping("/{caseId}")
    @Operation(summary = "Get case by ID", description = "Retrieves a case by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found")
    })
    public ResponseEntity<CaseResource> getCaseById(@PathVariable("caseId") UUID caseId) {
        var resource = caseQueryService.handle(new GetCaseByIdQuery(caseId))
                .map(CaseResourceFromEntityAssembler::toResourceFromEntity)
                .orElseThrow(() -> new IllegalStateException("Case not found"));

        return ResponseEntity.ok(resource);
    }

    /**
     * Retrieves all cases for a specific client.
     *
     * @param clientId the unique identifier of the client
     * @return a ResponseEntity with the list of CaseResource and HTTP status 200
     */
    @GetMapping("/clients/{clientId}")
    @Operation(summary = "Get cases by client ID", description = "Retrieves all cases for a specific client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cases retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<List<CaseResource>> getCasesByClient(@PathVariable("clientId") UUID clientId) {
        var list = caseQueryService.handle(new GetCasesByClientIdQuery(clientId))
                .stream().map(CaseResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves suggested cases for a lawyer.
     *
     * @param lawyerId the unique identifier of the lawyer
     * @return a ResponseEntity with the list of suggested CaseResource and HTTP status 200
     */
    @GetMapping("/suggested")
    @Operation(summary = "Get suggested cases", description = "Retrieves suggested cases for a lawyer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Suggested cases retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lawyer not found")
    })
    public ResponseEntity<List<CaseResource>> getSuggestedCases(@RequestParam UUID lawyerId) {
        var list = caseQueryService.handle(new GetSuggestedCasesQuery(lawyerId))
                .stream().map(CaseResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves cases by their status.
     *
     * @param status the status of the cases as a String
     * @return a ResponseEntity with the list of CaseResource and HTTP status 200
     */
    @GetMapping("/status")
    @Operation(summary = "Get cases by status", description = "Retrieves all cases with a specific status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cases retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    public ResponseEntity<List<CaseResource>> getCasesByStatus(@RequestParam String status) {
        var list = caseQueryService.handle(new GetCasesByStatusQuery(CaseStatus.valueOf(status)))
                .stream().map(CaseResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves all cases accepted by a specific lawyer.
     *
     * @param lawyerId the unique identifier of the lawyer
     * @return a ResponseEntity with the list of CaseResource and HTTP status 200
     */
    @GetMapping("/lawyer/{lawyerId}")
    @Operation(summary = "Get cases accepted by lawyer", description = "Retrieves all cases accepted by a specific lawyer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cases retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lawyer not found")
    })
    public ResponseEntity<List<CaseResource>> getCasesByLawyer(@PathVariable("lawyerId") UUID lawyerId) {
        var list = caseQueryService.handle(new GetCasesByLawyerIdQuery(lawyerId))
                .stream().map(CaseResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }
}
