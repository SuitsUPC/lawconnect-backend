package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentType;

import java.util.UUID;

/**
 * Resource representation for creating a comment.
 * Encapsulates the details required to create a new comment, including the case ID,
 * author ID, text content, and the type of the comment.
 *
 * @param caseId   the unique identifier of the case associated with the comment
 * @param authorId the unique identifier of the author of the comment
 * @param text     the content of the comment
 * @param type     the type of the comment (e.g., FINAL_REVIEW, GENERAL)
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record CreateCommentResource(
        UUID caseId,
        UUID authorId,
        String text,
        CommentType type
) { }
