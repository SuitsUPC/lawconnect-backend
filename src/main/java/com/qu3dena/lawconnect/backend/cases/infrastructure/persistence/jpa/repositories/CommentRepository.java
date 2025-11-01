package com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories;

import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Comment;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing {@link Comment} entities.
 * Extends {@link JpaRepository} to provide CRUD operations and custom queries.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Finds all comments associated with a specific legal case.
     *
     * @param caseId the unique identifier of the legal case
     * @return a list of comments linked to the given legal case
     */
    List<Comment> findByLegalCase_Id(UUID caseId);

    /**
     * Finds all comments associated with a specific lawyer and of a specific type.
     *
     * @param lawyerId the unique identifier of the lawyer
     * @param type     the type of the comments
     * @return a list of comments linked to the given lawyer and of the given type
     */
    List<Comment> findByLegalCase_AssignedLawyerIdAndType(UUID lawyerId, CommentType type);

}
