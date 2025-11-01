package com.qu3dena.lawconnect.backend.cases.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetFinalCommentsByLawyerIdQuery;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CommentType;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommentQueryServiceImplTest {

    @Mock
    CommentRepository repository;
    @InjectMocks
    CommentQueryServiceImpl service;

    @BeforeEach void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleGetFinalCommentByLawyerId_shouldReturnList() {
        // Arrange
        var lawyerId = UUID.randomUUID();

        when(repository.findByLegalCase_AssignedLawyerIdAndType(lawyerId, CommentType.FINAL_REVIEW))
                .thenReturn(List.of());

        var query = new GetFinalCommentsByLawyerIdQuery(lawyerId);

        // Act
        var result = service.handle(query);

        // Assert
        assertNotNull(result);
        verify(repository).findByLegalCase_AssignedLawyerIdAndType(lawyerId, CommentType.FINAL_REVIEW);
    }
}
