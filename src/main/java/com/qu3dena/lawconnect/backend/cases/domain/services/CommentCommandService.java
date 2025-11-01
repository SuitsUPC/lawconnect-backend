package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CreateCommentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.DeleteCommentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Comment;

import java.util.Optional;

/**
 * Service interface for handling comment-related commands.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface CommentCommandService {

    /**
     * Handles the creation of a comment.
     *
     * @param command the command containing the details of the comment to be created
     */
    Optional<Comment> handle(CreateCommentCommand command);

    /**
     * Handles the deletion of a comment.
     *
     * @param command the command containing the details of the comment to be deleted
     */
    void handle(DeleteCommentCommand command);
}
