package com.qu3dena.lawconnect.backend.cases.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CreateCommentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.DeleteCommentCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Comment;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.CommentCreatedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseTitle;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentText;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentType;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CaseRepository;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CommentRepository;
import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommentCommandServiceImplTest {

    @Mock
    CaseRepository caseRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    CommentCommandServiceImpl service;

    CaseAggregate case_;
    UUID caseId, authorId;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        caseId = UUID.randomUUID();
        authorId = UUID.randomUUID();

        case_ = CaseAggregate.create(UUID.randomUUID(),
                new CaseTitle("T"),
                new Description("D")
        );
    }

    @Test
    @DisplayName("")
    void handleCreateGeneralComment_shouldSaveAndPublish() {
        // Arrange
        case_.evaluation();

        when(caseRepository.findById(caseId))
                .thenReturn(Optional.of(case_));
        when(commentRepository.save(any()))
                .thenAnswer(i->i.getArgument(0));

        var command = new CreateCommentCommand(
                caseId, authorId,
                new CommentText("hi"), CommentType.GENERAL
        );

        // Act
        var result = service.handle(command);

        // Assert
        assertTrue(result.isPresent());
        verify(commentRepository).save(any());
        verify(eventPublisher).publishEvent(any(CommentCreatedEvent.class));
    }

    @Test
    @DisplayName("")
    void handleCreateFinalComment_onNonAccepted_shouldThrow() {
        // Arrange
        when(caseRepository.findById(caseId))
                .thenReturn(Optional.of(case_));

        var command = new CreateCommentCommand(
                caseId, authorId,
                new CommentText("final"), CommentType.FINAL_REVIEW
        );

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> service.handle(command));
    }

    @Test
    @DisplayName("handleDelete should delete the comment when IDs match")
    void handleDelete_shouldRemoveComment() {
        // Arrange
        var saved = mock(Comment.class);
        var commentId = 1L;
        var authorId = UUID.randomUUID();

        when(saved.getId())
                .thenReturn(commentId);
        when(saved.getAuthorId())
                .thenReturn(authorId);
        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(saved));

        // Act
        service.handle(new DeleteCommentCommand(commentId, authorId));

        // Assert
        verify(commentRepository).delete(saved);
    }

}
