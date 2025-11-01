package com.qu3dena.lawconnect.backend.cases.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Comment;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetCommentsByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetFinalCommentsByLawyerIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentType;
import com.qu3dena.lawconnect.backend.cases.domain.services.CommentQueryService;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation of the {@code CommentQueryService} interface.
 * Handles queries related to retrieving comments for cases.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
public class CommentQueryServiceImpl implements CommentQueryService {

    private final CommentRepository commentRepository;

    /**
     * Constructs a {@code CommentQueryServiceImpl} with the specified {@code CommentRepository}.
     *
     * @param commentRepository the repository used for accessing comment data
     */
    public CommentQueryServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Handles the {@code GetCommentsByCaseIdQuery} to retrieve all comments associated with a case.
     *
     * @param query the query containing the case identifier
     * @return a list of {@code Comment} entities for the specified case
     */
    @Override
    public List<Comment> handle(GetCommentsByCaseIdQuery query) {
        return commentRepository.findByLegalCase_Id(query.caseId());
    }

    /**
     * Handles the {@code GetFinalCommentsByLawyerIdQuery} to retrieve final review comments for a lawyer.
     *
     * @param query the query containing the lawyer identifier
     * @return a list of {@code Comment} entities of type FINAL_REVIEW for the specified lawyer
     */
    @Override
    public List<Comment> handle(GetFinalCommentsByLawyerIdQuery query) {
        return commentRepository.findByLegalCase_AssignedLawyerIdAndType(query.lawyerId(), CommentType.FINAL_REVIEW);
    }
}
