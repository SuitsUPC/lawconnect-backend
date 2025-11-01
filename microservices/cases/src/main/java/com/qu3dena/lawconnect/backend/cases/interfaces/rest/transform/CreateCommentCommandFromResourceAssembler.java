package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CreateCommentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentText;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateCommentResource;

public class CreateCommentCommandFromResourceAssembler {

    public static CreateCommentCommand toCommandFromResource(CreateCommentResource resource) {
        return new CreateCommentCommand(
                resource.caseId(),
                resource.authorId(),
                new CommentText(resource.text()),
                resource.type()
        );
    }
}
