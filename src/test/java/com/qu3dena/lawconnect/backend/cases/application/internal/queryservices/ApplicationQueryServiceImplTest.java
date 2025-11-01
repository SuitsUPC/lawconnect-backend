package com.qu3dena.lawconnect.backend.cases.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.queries.GetApplicationsByCaseIdQuery;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.ApplicationRepository;
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

public class ApplicationQueryServiceImplTest {

    @Mock
    ApplicationRepository repository;
    @InjectMocks
    ApplicationQueryServiceImpl service;

    @BeforeEach void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("")
    void handleGetByCaseId_shouldReturnList() {
        // Arrange
        var caseId = UUID.randomUUID();

        when(repository.findByLegalCase_Id(caseId))
                .thenReturn(List.of());

        var query = new GetApplicationsByCaseIdQuery(caseId);

        // Act
        var result = service.handle(query);

        // Assert
        assertNotNull(result);
        verify(repository).findByLegalCase_Id(caseId);
    }
}
