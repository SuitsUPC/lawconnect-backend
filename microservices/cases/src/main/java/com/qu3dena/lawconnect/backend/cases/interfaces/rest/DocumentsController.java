package com.qu3dena.lawconnect.backend.cases.interfaces.rest;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.UploadDocumentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetDocumentsByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.services.DocumentCommandService;
import com.qu3dena.lawconnect.backend.cases.domain.services.DocumentQueryService;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.DocumentResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.UploadDocumentResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.DocumentResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/cases/{caseId}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Documents", description = "Case Document Management Endpoints")
public class DocumentsController {

    private final DocumentCommandService commandService;
    private final DocumentQueryService queryService;

    public DocumentsController(DocumentCommandService commandService, DocumentQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload document to case")
    public ResponseEntity<DocumentResource> uploadDocument(
            @PathVariable String caseId,
            @RequestParam String uploadedBy,
            @RequestBody UploadDocumentResource resource
    ) {
        var command = new UploadDocumentCommand(
                UUID.fromString(caseId),
                UUID.fromString(uploadedBy),
                resource.filename(),
                resource.fileUrl(),
                resource.fileSize(),
                resource.fileType()
        );

        var document = commandService.handle(command);

        if (document.isEmpty())
            return ResponseEntity.badRequest().build();

        var documentResource = DocumentResourceFromEntityAssembler.toResourceFromEntity(document.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(documentResource);
    }

    @GetMapping
    @Operation(summary = "Get all documents for a case")
    public ResponseEntity<List<DocumentResource>> getDocumentsByCase(@PathVariable String caseId) {
        var query = new GetDocumentsByCaseIdQuery(UUID.fromString(caseId));
        var documents = queryService.handle(query);

        var documentResources = documents.stream()
                .map(DocumentResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentResources);
    }
}

