package com.qu3dena.lawconnect.backend.cases.domain.model.events;

import java.util.UUID;

/**
 * Event representing the creation of a comment.
 *
 * @param caseId    the unique identifier of the case associated with the comment
 * @param commentId the unique identifier of the created comment
 * @param authorId  the unique identifier of the author who created the comment
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record CommentCreatedEvent(
        UUID caseId,
        Long commentId,
        UUID authorId
) {
}
