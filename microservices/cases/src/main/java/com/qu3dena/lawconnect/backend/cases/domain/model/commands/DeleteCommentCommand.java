package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import java.util.UUID;

/**
 * Command for deleting a comment.
 *
 * @param commentId the ID of the comment to be deleted
 * @param authorId the ID of the author requesting the deletion
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record DeleteCommentCommand(Long commentId, UUID authorId) {
}
