package com.qu3dena.lawconnect.backend.cases.domain.model.queries;

import java.util.UUID;

/**
 * Query to retrieve all final comments associated with a specific lawyer.
 * <p>
 * This query is used to fetch comments of type {@code FINAL_REVIEW} that are linked
 * to a particular lawyer, identified by their unique ID.
 * </p>
 *
 * @param lawyerId the unique identifier of the lawyer whose final comments are to be retrieved
 *
 * @author LawConnect Team
 * @since 1.0
 */
public record GetFinalCommentsByLawyerIdQuery(UUID lawyerId) {
}
