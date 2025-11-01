package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import java.util.UUID;

/**
 * Resource representation for deleting a comment.
 * Encapsulates the details required to delete a comment, including the comment ID
 * and the author ID of the comment.
 *
 * @param commentId the unique identifier of the comment to be deleted
 * @param authorId  the unique identifier of the author of the comment
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record DeleteCommentResource(
        Long commentId,
        UUID authorId
) { }
