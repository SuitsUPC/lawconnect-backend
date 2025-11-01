package com.qu3dena.lawconnect.backend.cases.domain.services;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Comment;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetCommentsByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetFinalCommentsByLawyerIdQuery;

import java.util.List;

/**
 * Service interface for handling comment-related queries.
 *
 * @author LawConnect Team
 * @since 1.0
 */
public interface CommentQueryService {

    /**
     * Handles the retrieval of comments associated with a specific case.
     *
     * @param query the query containing the details of the case whose comments are being retrieved
     * @return a list of comments associated with the specified case
     */
    List<Comment> handle(GetCommentsByCaseIdQuery query);

    List<Comment> handle(GetFinalCommentsByLawyerIdQuery query);
}
