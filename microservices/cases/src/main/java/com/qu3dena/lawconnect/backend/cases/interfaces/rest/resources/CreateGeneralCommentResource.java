package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

import java.util.UUID;

/**
 * Resource representation for creating a general comment.
 * Encapsulates the details required to create a general comment, including the case ID,
 * author ID, and the text content of the comment.
 *
 * @param caseId   the unique identifier of the case associated with the comment
 * @param authorId the unique identifier of the author of the comment
 * @param text     the content of the comment
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record CreateGeneralCommentResource(
        UUID caseId,
        UUID authorId,
        String text
) {}
