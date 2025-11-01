package com.qu3dena.lawconnect.backend.cases.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetInvitationsByLawyerIdQuery;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.InvitationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InvitationQueryServiceImplTest {

    @Mock
    InvitationRepository repository;
    @InjectMocks
    InvitationQueryServiceImpl service;

    @BeforeEach void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("")
    void handleGetByLawyerId_shouldReturnList() {
        // Arrange
        var lawyerId = UUID.randomUUID();

        when(repository.findByLawyerId(lawyerId))
                .thenReturn(List.of());

        var query = new GetInvitationsByLawyerIdQuery(lawyerId);

        // Act
        var result = service.handle(query);

        // Assert
        assertNotNull(result);
        verify(repository).findByLawyerId(lawyerId);
    }
}
