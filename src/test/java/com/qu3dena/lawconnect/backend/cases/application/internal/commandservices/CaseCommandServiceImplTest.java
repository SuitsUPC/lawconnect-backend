package com.qu3dena.lawconnect.backend.cases.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CancelCaseCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CloseCaseCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.CreateCaseCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.CaseCanceledEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.CaseCreatedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseTitle;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CaseRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CaseCommandServiceImplTest {

    @Mock
    CaseRepository repository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @InjectMocks
    CaseCommandServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("handle(CreateCaseCommand) should save and publish CaseCreatedEvent")
    void handleCreate_shouldSaveAndPublish() {
        // Arrange
        var clientId = UUID.randomUUID();
        var title = new CaseTitle("Test Case");
        var description = new Description("This is a test case description.");

        var command = new CreateCaseCommand(clientId, title, description);

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = service.handle(command);

        // Assert
        assertTrue(result.isPresent());
        verify(repository).save(any(CaseAggregate.class));
        verify(eventPublisher).publishEvent(any(CaseCreatedEvent.class));
    }

    @Test
    @DisplayName("handle(CloseCaseCommand) with invalid client should throw IllegalArgumentException")
    void handleClose_withBadClient_shouldThrow() {
        // Arrange
        var caseId = UUID.randomUUID();
        var title = new CaseTitle("Test Case");
        var description = new Description("This is a test case description.");

        var command = new CloseCaseCommand(caseId, UUID.randomUUID());

        var case_ = CaseAggregate.create(UUID.randomUUID(), title, description);
        when(repository.findById(caseId)).thenReturn(
                Optional.of(case_)
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.handle(command));
    }

    @Test
    @DisplayName("handle(CancelCaseCommand) with valid client should cancel and publish CaseCanceledEvent")
    void handleCancel_withValidClient_shouldCancelAndPublish() {
        // Arrange
        var clientId = UUID.randomUUID();
        var title = new CaseTitle("Test Case");
        var description = new Description("This is a test case description.");

        var case_ = CaseAggregate.create(clientId, title, description);

        when(repository.findById(case_.getId()))
                .thenReturn(Optional.of(case_));

        when(repository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        var command = new CancelCaseCommand(case_.getId(), clientId);

        // Act
        var result = service.handle(command);

        // Assert
        assertTrue(result.isPresent(), "The result must be present");
        assertEquals(CaseStatus.CANCELED,
                result.get().getStatus(), "The status must be CANCELED after cancellation");
        verify(eventPublisher).publishEvent(any(CaseCanceledEvent.class));
    }
}
