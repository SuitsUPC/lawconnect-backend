package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentType;

import java.time.Instant;
import java.util.UUID;

/**
 * Resource representation of a comment.
 * Encapsulates the details of a comment, including its ID, associated case ID,
 * author ID, type, content, and the timestamp when it was created.
 *
 * @param commentId  the unique identifier of the comment
 * @param caseId     the unique identifier of the case associated with the comment
 * @param authorId   the unique identifier of the author of the comment
 * @param type       the type of the comment (e.g., FINAL_REVIEW, GENERAL)
 * @param comment    the content of the comment
 * @param createdAt  the timestamp when the comment was created
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record CommentResource(
        Long commentId,
        UUID caseId,
        UUID authorId,
        CommentType type,
        String comment,
        Instant createdAt
) {
}
