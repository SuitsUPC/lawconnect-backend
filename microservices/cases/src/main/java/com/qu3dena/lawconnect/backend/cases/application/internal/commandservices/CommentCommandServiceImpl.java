package com.qu3dena.lawconnect.backend.cases.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CreateCommentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.DeleteCommentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Comment;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.CommentCreatedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentType;
import com.qu3dena.lawconnect.backend.cases.domain.services.CommentCommandService;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CaseRepository;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CommentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service implementation for handling comment-related commands.
 * </p>
 * Provides methods to create and delete comments, while ensuring
 * business rules and domain constraints are respected.
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Service
public class CommentCommandServiceImpl implements CommentCommandService {

    private final CaseRepository caseRepository;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructs an instance of {@link CommentCommandServiceImpl}.
     *
     * @param commentRepository the repository for managing comments
     * @param caseRepository    the repository for managing cases
     * @param eventPublisher    the event publisher for domain events
     */
    public CommentCommandServiceImpl(CommentRepository commentRepository, CaseRepository caseRepository, ApplicationEventPublisher eventPublisher) {
        this.commentRepository = commentRepository;
        this.caseRepository = caseRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles the creation of a new comment.
     * Validates the case status for final review comments, creates a new comment,
     * associates it with the case, saves it, and publishes a comment created event.
     *
     * @param command the command containing the details for creating the comment
     * @return an {@link Optional} containing the created comment, if successful
     */
    @Override
    public Optional<Comment> handle(CreateCommentCommand command) {
        // 1). Retrieve the case by its ID or throw an exception if not found
        var maybeCase = caseRepository.findById(command.caseId())
                .orElseThrow(() -> new IllegalArgumentException("No case found with id: " + command.caseId()));

        // 2). Check if the comment type is FINAL_REVIEW and ensure the case status is ACCEPTED
        if (command.type() == CommentType.FINAL_REVIEW && maybeCase.getStatus() != CaseStatus.ACCEPTED)
            throw new IllegalStateException("You can only leave a final comment on an Accepted case");

        // 3). Create a new comment, associate it with the case, and save it to the repository
        var comment = Comment.create(command.text(), maybeCase, command.authorId(), command.type());
        var saved = commentRepository.save(comment);

        // 4). Publish a CommentCreatedEvent to notify other parts of the system
        eventPublisher.publishEvent(new CommentCreatedEvent(
                saved.getCaseId(),
                saved.getId(),
                saved.getAuthorId()
        ));

        return Optional.of(saved);
    }

    /**
     * Handles the deletion of a comment.
     * Retrieves the comment by its ID and deletes it from the repository.
     *
     * @param command the command containing the details for deleting the comment
     */
    @Override
    public void handle(DeleteCommentCommand command) {

        // 1). Retrieve the comment by its ID or throw an exception if not found
        var maybeComment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new IllegalArgumentException("No comment found with id: " + command.commentId()));

        // 2). Delete the comment from the repository
        commentRepository.delete(maybeComment);
    }
}
