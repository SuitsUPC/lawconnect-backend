package com.qu3dena.lawconnect.backend.cases.interfaces.rest;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.AcceptInvitationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.RejectInvitationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetInvitationsByLawyerIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.services.InvitationCommandService;
import com.qu3dena.lawconnect.backend.cases.domain.services.InvitationQueryService;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.InvitationResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.InviteLawyerResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.InvitationResourceFromEntityAssembler;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.InviteLawyerCommandFromResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * REST controller for managing invitations.
 * Provides endpoints to invite a lawyer, retrieve invitations, and accept or reject an invitation.
 *
 * <p>This controller uses {@code InvitationCommandService} and {@code InvitationQueryService} for handling invitation operations.</p>
 *
 * @see InvitationCommandService
 * @see InvitationQueryService
 *
 * @author LawConnect Team
 * @since 1.0
 */
@RestController
@RequestMapping(value = "/api/v1/invitations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Invitations", description = "Invitations Management Endpoints")
public class InvitationsController {

    private final InvitationCommandService invitationCommandService;
    private final InvitationQueryService invitationQueryService;

    /**
     * Constructs an InvitationsController with the specified command and query services.
     *
     * @param invitationCommandService the service to handle invitation commands
     * @param invitationQueryService   the service to handle invitation queries
     */
    public InvitationsController(
            InvitationCommandService invitationCommandService,
            InvitationQueryService invitationQueryService
    ) {
        this.invitationCommandService = invitationCommandService;
        this.invitationQueryService = invitationQueryService;
    }

    /**
     * Invites a lawyer to join a case.
     *
     * @param resource the invitation details provided as an InviteLawyerResource
     * @return a ResponseEntity with the created InvitationResource and HTTP status 201
     */
    @PostMapping
    @Operation(summary = "Invite a lawyer", description = "Creates a new invitation for a lawyer to join a case.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Invitation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<InvitationResource> inviteLawyer(
            @RequestBody InviteLawyerResource resource
    ) {
        var command = InviteLawyerCommandFromResourceAssembler.toCommandFromResource(resource);
        var created = invitationCommandService.handle(command)
                .map(InvitationResourceFromEntityAssembler::toResourceFromEntity)
                .orElseThrow(() -> new IllegalStateException("Failed to invite lawyer"));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieves all invitations for a given lawyer.
     *
     * @param lawyerId the unique identifier of the lawyer
     * @return a ResponseEntity with a list of InvitationResource and HTTP status 200
     */
    @GetMapping
    @Operation(summary = "Get invitations by lawyer", description = "Retrieves all invitations for a specific lawyer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lawyer not found")
    })
    public ResponseEntity<List<InvitationResource>> getInvitations(@RequestParam UUID lawyerId) {
        var list = invitationQueryService.handle(
                        new GetInvitationsByLawyerIdQuery(lawyerId)
                ).stream().map(InvitationResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Accepts an invitation for a lawyer to join a case.
     *
     * @param invitationId the unique identifier of the invitation to accept
     * @param lawyerId     the unique identifier of the lawyer accepting the invitation
     * @return a ResponseEntity with the updated InvitationResource and HTTP status 200
     */
    @PutMapping("/{invitationId}/accept")
    @Operation(summary = "Accept an invitation", description = "Accepts an invitation for a lawyer to join a case.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation accepted successfully"),
            @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    public ResponseEntity<InvitationResource> acceptInvitation(
            @PathVariable Long invitationId,
            @RequestParam UUID lawyerId
    ) {
        var command = new AcceptInvitationCommand(invitationId, lawyerId);
        var updated = invitationCommandService.handle(command)
                .map(InvitationResourceFromEntityAssembler::toResourceFromEntity)
                .orElseThrow(() -> new IllegalStateException("Failed to accept invitation"));
        return ResponseEntity.ok(updated);
    }

    /**
     * Rejects an invitation for a lawyer to join a case.
     *
     * @param invitationId the unique identifier of the invitation to reject
     * @param lawyerId     the unique identifier of the lawyer rejecting the invitation
     * @return a ResponseEntity with the updated InvitationResource and HTTP status 200
     * @throws IllegalStateException if the invitation rejection fails
     */
    @PutMapping("/{invitationId}/reject")
    @Operation(summary = "Reject an invitation", description = "Rejects an invitation for a lawyer to join a case.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation rejected successfully"),
            @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    public ResponseEntity<InvitationResource> rejectInvitation(
            @PathVariable Long invitationId,
            @RequestParam UUID lawyerId
    ) {
        var command = new RejectInvitationCommand(invitationId, lawyerId);
        var updated = invitationCommandService.handle(command)
                .map(InvitationResourceFromEntityAssembler::toResourceFromEntity)
                .orElseThrow(() -> new IllegalStateException("Failed to reject invitation"));
        return ResponseEntity.ok(updated);
    }
}
