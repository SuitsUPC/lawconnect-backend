package com.qu3dena.lawconnect.backend.cases.interfaces.rest;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.DeleteCommentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetCommentsByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetFinalCommentsByLawyerIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.services.CommentCommandService;
import com.qu3dena.lawconnect.backend.cases.domain.services.CommentQueryService;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CommentResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateFinalCommentResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateGeneralCommentResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.CommentResourceFromEntityAssembler;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.CreateFinalCommentFromResourceAssembler;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.CreateGeneralCommentFromResourceAssembler;
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
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing comments.
 * Provides endpoints to get, create, and delete comments associated with cases.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@RestController
@RequestMapping(value = "/api/v1/comments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Comments", description = "Comments Management Endpoints")
public class CommentsController {

    private final CommentCommandService commentCommandService;
    private final CommentQueryService commentQueryService;

    /**
     * Constructs a CommentsController with the specified command and query services.
     *
     * @param commentCommandService the service to handle comment commands
     * @param commentQueryService   the service to handle comment queries
     */
    public CommentsController(CommentCommandService commentCommandService, CommentQueryService commentQueryService) {
        this.commentCommandService = commentCommandService;
        this.commentQueryService = commentQueryService;
    }

    /**
     * Retrieves all comments associated with a specific case.
     *
     * @param caseId the unique identifier of the case
     * @return a ResponseEntity with the list of CommentResource and HTTP status 200
     */
    @GetMapping
    @Operation(summary = "Get comments by case ID", description = "Retrieves all comments associated with a specific case.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found")
    })
    public ResponseEntity<List<CommentResource>> getComments(@RequestParam UUID caseId) {
        var list = commentQueryService.handle(
                        new GetCommentsByCaseIdQuery(caseId)
                ).stream().map(CommentResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves all final comments associated with a specific lawyer.
     *
     * @param lawyerId the unique identifier of the lawyer
     * @return a ResponseEntity with the list of CommentResource and HTTP status 200
     */
    @GetMapping("/lawyer/{lawyerId}/final")
    @Operation(summary = "Final comments for a lawyer", description = "Retrieves all final comments associated with a specific lawyer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Final comments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lawyer not found")
    })
    public ResponseEntity<List<CommentResource>> getFinalCommentsByLawyer(
            @PathVariable("lawyerId") UUID lawyerId
    ) {
        var comments = commentQueryService
                .handle(new GetFinalCommentsByLawyerIdQuery(lawyerId))
                .stream()
                .map(CommentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(comments);
    }

    /**
     * Creates a general comment in a case.
     *
     * @param resource the resource containing general comment details
     * @return a ResponseEntity with the created CommentResource and HTTP status 201
     * @throws ResponseStatusException if the comment creation fails
     */
    @PostMapping("/general")
    @Operation(summary = "Create a general comment in a case", description = "Creates a general comment in a case, which can be used for various purposes such as updates or discussions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "General comment created successfully"),
            @ApiResponse(responseCode = "500", description = "Error creating general comment")
    })
    public ResponseEntity<CommentResource> createGeneral(
            @RequestBody CreateGeneralCommentResource resource) {

        var command = CreateGeneralCommentFromResourceAssembler.toCommandFromResource(resource);

        var comment = commentCommandService.handle(command)
                .map(CommentResourceFromEntityAssembler::toResourceFromEntity)
                .orElseThrow(() -> new IllegalStateException("Failed to create general comment"));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(comment);
    }

    /**
     * Creates a final comment in a case.
     *
     * @param resource the resource containing final comment details
     * @return a ResponseEntity with the created CommentResource and HTTP status 201
     * @throws ResponseStatusException if the comment creation fails
     */
    @PostMapping("/final")
    @Operation(summary = "Create a final comment in a case", description = "Creates a final comment in a case, which is typically used for concluding remarks or reviews.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Final comment created successfully"),
            @ApiResponse(responseCode = "500", description = "Error creating final comment")
    })
    public ResponseEntity<CommentResource> createFinal(
            @RequestBody CreateFinalCommentResource resource
    ) {
        try {
            var command = CreateFinalCommentFromResourceAssembler.toCommandFromResource(resource);

            var comment = commentCommandService.handle(command)
                    .map(CommentResourceFromEntityAssembler::toResourceFromEntity)
                    .orElseThrow(() -> new IllegalStateException("Failed to create final comment"));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(comment);

        } catch (IllegalStateException ex) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Deletes a comment by its ID and author ID.
     *
     * @param commentId the unique identifier of the comment to delete
     * @param authorId  the unique identifier of the comment author
     * @return a ResponseEntity with HTTP status 204 if deletion is successful
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete a comment", description = "Deletes a comment by its ID and author ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    public ResponseEntity<Void> deleteComment(
            @PathVariable("commentId") Long commentId,
            @RequestParam UUID authorId
    ) {
        commentCommandService.handle(new DeleteCommentCommand(commentId, authorId));
        return ResponseEntity.noContent().build();
    }
}
