package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CreateCommentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentText;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentType;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateGeneralCommentResource;

public class CreateGeneralCommentFromResourceAssembler {
    public static CreateCommentCommand toCommandFromResource(CreateGeneralCommentResource resource) {
        return new CreateCommentCommand(
                resource.caseId(),
                resource.authorId(),
                new CommentText(resource.text()),
                CommentType.GENERAL
        );
    }
}
