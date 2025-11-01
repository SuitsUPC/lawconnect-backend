package com.qu3dena.lawconnect.backend.cases.domain.model.commands;

import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentText;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentType;

import java.util.UUID;

/**
 * Command to create a comment in a case.
 *
 * @param caseId the unique identifier of the case where the comment will be added
 * @param authorId the unique identifier of the author creating the comment
 * @param text the text content of the comment
 * @param type the type of the comment (e.g., general, question, etc.)
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record CreateCommentCommand(UUID caseId, UUID authorId, CommentText text, CommentType type) {
}
