package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CreateCommentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentText;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentType;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CreateFinalCommentResource;

public class CreateFinalCommentFromResourceAssembler {
    public static CreateCommentCommand toCommandFromResource(CreateFinalCommentResource resource) {
        return new CreateCommentCommand(
                resource.caseId(),
                resource.authorId(),
                new CommentText(resource.text()),
                CommentType.FINAL_REVIEW
        );
    }
}
