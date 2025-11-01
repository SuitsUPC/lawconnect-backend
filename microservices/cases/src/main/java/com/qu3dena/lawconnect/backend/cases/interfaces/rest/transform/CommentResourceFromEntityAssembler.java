package com.qu3dena.lawconnect.backend.cases.interfaces.rest.transform;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Comment;
import com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources.CommentResource;

public class CommentResourceFromEntityAssembler {
    public static CommentResource toResourceFromEntity(Comment entity) {
        return new CommentResource(
                entity.getId(),
                entity.getLegalCase().getId(),
                entity.getAuthorId(),
                entity.getType(),
                entity.getText().comment(),
                entity.getCreatedAt().toInstant()
        );
    }
}
