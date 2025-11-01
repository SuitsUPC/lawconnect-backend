package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.DeleteCommentCommand;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.DeleteCommentResource;

public class DeleteCommentCommandFromResourceAssembler {
    public static DeleteCommentCommand toCommandFromResource(DeleteCommentResource resource) {
        return new DeleteCommentCommand(
                resource.commentId(),
                resource.authorId()
        );
    }
}
