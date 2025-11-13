package com.qu3dena.lawconnect.backend.cases.interfaces.rest;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.UploadDocumentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetCaseByIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetDocumentsByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.services.CaseQueryService;
import com.qu3dena.lawconnect.backend.cases.domain.services.DocumentCommandService;
import com.qu3dena.lawconnect.backend.cases.domain.services.DocumentQueryService;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.DocumentResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.UploadDocumentResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.DocumentResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/cases/{caseId}/documents")
@Tag(name = "Documents", description = "Case Document Management Endpoints")
public class DocumentsController {

    private final DocumentCommandService commandService;
    private final DocumentQueryService queryService;
    private final CaseQueryService caseQueryService;

    public DocumentsController(DocumentCommandService commandService, DocumentQueryService queryService, CaseQueryService caseQueryService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.caseQueryService = caseQueryService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload file to case")
    public ResponseEntity<DocumentResource> uploadDocumentFile(
            @PathVariable String caseId,
            @RequestParam String uploadedBy,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // Validar que el caso existe y permite subir archivos
            var caseQuery = new GetCaseByIdQuery(UUID.fromString(caseId));
            var maybeCase = caseQueryService.handle(caseQuery);
            
            if (maybeCase.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CaseAggregate caseAggregate = maybeCase.get();
            CaseStatus status = caseAggregate.getStatus();
            
            // Solo permitir subir archivos si el caso NO está CLOSED o CANCELED
            // ACCEPTED permite subir archivos porque aún se hace seguimiento del caso
            if (status == CaseStatus.CLOSED || status == CaseStatus.CANCELED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Error-Message", 
                            status == CaseStatus.CLOSED ? "Este caso está cerrado y no permite más modificaciones" :
                            "Este caso está cancelado y no permite más modificaciones")
                        .build();
            }
            
            // Validar archivo
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null || 
                (!contentType.equals("application/pdf") && 
                 !contentType.equals("application/msword") && 
                 !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") &&
                 !contentType.startsWith("image/"))) {
                return ResponseEntity.badRequest().build();
            }

            // Validar tamaño (10MB)
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                return ResponseEntity.badRequest().build();
            }

            // Crear directorio de almacenamiento si no existe
            String uploadDir = System.getProperty("user.home") + "/lawconnect-documents";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generar nombre único para el archivo
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Guardar archivo
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Crear URL del archivo para descarga
            String fileUrl = "/api/v1/cases/" + caseId + "/documents/download/" + uniqueFilename;

            // Crear comando para guardar en BD
            var command = new UploadDocumentCommand(
                    UUID.fromString(caseId),
                    UUID.fromString(uploadedBy),
                    originalFilename != null ? originalFilename : "document",
                    fileUrl,
                    file.getSize(),
                    contentType != null ? contentType : "application/octet-stream"
            );

            var document = commandService.handle(command);

            if (document.isEmpty()) {
                // Si falla guardar en BD, eliminar archivo
                Files.deleteIfExists(filePath);
                return ResponseEntity.badRequest().build();
            }

            var documentResource = DocumentResourceFromEntityAssembler.toResourceFromEntity(document.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(documentResource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload document metadata to case")
    public ResponseEntity<DocumentResource> uploadDocument(
            @PathVariable String caseId,
            @RequestParam String uploadedBy,
            @RequestBody UploadDocumentResource resource
    ) {
        // Validar que el caso existe y permite subir archivos
        var caseQuery = new GetCaseByIdQuery(UUID.fromString(caseId));
        var maybeCase = caseQueryService.handle(caseQuery);
        
        if (maybeCase.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        CaseAggregate caseAggregate = maybeCase.get();
        CaseStatus status = caseAggregate.getStatus();
        
        // Solo permitir subir archivos si el caso NO está CLOSED o CANCELED
        // ACCEPTED permite subir archivos porque aún se hace seguimiento del caso
        if (status == CaseStatus.CLOSED || status == CaseStatus.CANCELED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-Error-Message", 
                        status == CaseStatus.CLOSED ? "Este caso está cerrado y no permite más modificaciones" :
                        "Este caso está cancelado y no permite más modificaciones")
                    .build();
        }
        
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

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all documents for a case")
    public ResponseEntity<List<DocumentResource>> getDocumentsByCase(@PathVariable String caseId) {
        var query = new GetDocumentsByCaseIdQuery(UUID.fromString(caseId));
        var documents = queryService.handle(query);

        var documentResources = documents.stream()
                .map(DocumentResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(documentResources);
    }

    @GetMapping(value = "/download/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Download document file")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable String caseId,
            @PathVariable String filename
    ) {
        try {
            String uploadDir = System.getProperty("user.home") + "/lawconnect-documents";
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

