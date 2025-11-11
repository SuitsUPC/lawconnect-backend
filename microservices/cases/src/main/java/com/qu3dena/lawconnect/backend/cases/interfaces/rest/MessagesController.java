package com.qu3dena.lawconnect.backend.cases.interfaces.rest;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.SendMessageCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetMessagesByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.services.MessageCommandService;
import com.qu3dena.lawconnect.backend.cases.domain.services.MessageQueryService;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.MessageResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.SendMessageResource;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform.MessageResourceFromEntityAssembler;
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
@RequestMapping(value = "/api/v1/cases/{caseId}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Messages", description = "Case Messaging Endpoints")
public class MessagesController {

    private final MessageCommandService commandService;
    private final MessageQueryService queryService;

    public MessagesController(MessageCommandService commandService, MessageQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Send message in case")
    public ResponseEntity<MessageResource> sendMessage(
            @PathVariable String caseId,
            @RequestParam String senderId,
            @RequestBody SendMessageResource resource
    ) {
        var command = new SendMessageCommand(
                UUID.fromString(caseId),
                UUID.fromString(senderId),
                resource.content()
        );

        var message = commandService.handle(command);

        if (message.isEmpty())
            return ResponseEntity.badRequest().build();

        var messageResource = MessageResourceFromEntityAssembler.toResourceFromEntity(message.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(messageResource);
    }

    @GetMapping
    @Operation(summary = "Get all messages for a case")
    public ResponseEntity<List<MessageResource>> getMessagesByCase(@PathVariable String caseId) {
        var query = new GetMessagesByCaseIdQuery(UUID.fromString(caseId));
        var messages = queryService.handle(query);

        var messageResources = messages.stream()
                .map(MessageResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageResources);
    }
}

