package com.qu3dena.lawconnect.backend.cases.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.AcceptInvitationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.InviteLawyerCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Invitation;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.InvitationAcceptedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.LawyerInvitedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseTitle;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.InvitationStatus;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.CaseRepository;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.InvitationRepository;
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

public class InvitationCommandServiceImplTest {

    @Mock
    CaseRepository caseRepository;
    @Mock
    InvitationRepository invitationRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    InvitationCommandServiceImpl service;

    CaseAggregate case_;
    UUID caseId, clientId, lawyerId;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        caseId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        lawyerId = UUID.randomUUID();

        case_ = CaseAggregate.create(
                clientId,
                new CaseTitle("Test Case"),
                new Description("Test Description")
        );
    }

    @Test
    @DisplayName("")
    void handleInvite_onOpenCase_shouldMoveToEvaluationAndSaveInvitation() {
        // Arrange
        when(caseRepository.findById(caseId))
                .thenReturn(Optional.of(case_));
        when(invitationRepository.findByLawyerIdAndLegalCase_Id(lawyerId, caseId))
                .thenReturn(Optional.empty());
        when(invitationRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        var command = new InviteLawyerCommand(caseId, lawyerId, clientId);

        // Act
        var result = service.handle(command);

        // Assert
        assertTrue(result.isPresent(), "The invitation must be returned");
        assertEquals(CaseStatus.EVALUATION, case_.getStatus(),
                "The case must move to EVALUATION after inviting");

        verify(invitationRepository).save(any(Invitation.class));
        verify(eventPublisher).publishEvent(any(LawyerInvitedEvent.class));
    }

    @Test
    @DisplayName("")
    void handleInvite_whenAlreadyInvited_shouldThrow() {
        // Arrange
        when(caseRepository.findById(caseId))
                .thenReturn(Optional.of(case_));
        when(invitationRepository.findByLawyerIdAndLegalCase_Id(lawyerId, caseId))
                .thenReturn(Optional.of(mock(Invitation.class)));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> service.handle(new InviteLawyerCommand(caseId, lawyerId, clientId)));
    }

    @Test
    @DisplayName("")
    void handleAccept_onValidPending_shouldAcceptAndPublish() {
        // Arrange
        case_.evaluation();
        var invitation = Invitation.create(case_, lawyerId, InvitationStatus.PENDING);

        when(invitationRepository.findById(1L))
                .thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(caseRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        var command = new AcceptInvitationCommand(1L, lawyerId);

        // Act
        var result = service.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(InvitationStatus.ACCEPTED, result.get().getStatus());
        assertEquals(CaseStatus.ACCEPTED, case_.getStatus());

        verify(eventPublisher).publishEvent(any(InvitationAcceptedEvent.class));
    }

    @Test
    @DisplayName("")
    void handleAccept_whenNotInEvaluation_shouldThrow() {
        // Arrange
        var invitation = Invitation.create(case_, lawyerId, InvitationStatus.PENDING);

        when(invitationRepository.findById(1L))
                .thenReturn(Optional.of(invitation));

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> service.handle(new AcceptInvitationCommand(1L, lawyerId)),
                "Accepting an invitation when the case is not in EVALUATION must fail");
    }
}
