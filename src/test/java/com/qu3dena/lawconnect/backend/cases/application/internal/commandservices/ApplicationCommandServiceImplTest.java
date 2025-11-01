package com.qu3dena.lawconnect.backend.cases.application.internal.commandservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.AcceptApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.RejectApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.commands.SubmitApplicationCommand;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Application;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.ApplicationAcceptedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.ApplicationRejectedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.events.ApplicationSubmittedEvent;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.ApplicationStatus;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseTitle;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.ApplicationRepository;
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

public class ApplicationCommandServiceImplTest {

    @Mock
    CaseRepository caseRepository;
    @Mock
    ApplicationRepository applicationRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    ApplicationCommandServiceImpl service;

    CaseAggregate case_;
    Application application;
    UUID caseId, lawyerId, clientId;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        caseId = UUID.randomUUID();
        lawyerId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        case_ = CaseAggregate.create(clientId,
                new CaseTitle("T"),
                new Description("D")
        );
    }

    @Test
    @DisplayName("")
    void handleSubmit_onOpenCase_shouldEvaluationAndSaveApplication() {
        // Arrange
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(case_));
        when(applicationRepository.save(any())).thenAnswer(i->i.getArgument(0));

        var command = new SubmitApplicationCommand(caseId, lawyerId);

        // Act
        var result = service.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ApplicationStatus.SUBMITTED, result.get().getStatus());
        assertEquals(CaseStatus.EVALUATION, case_.getStatus());
        verify(eventPublisher).publishEvent(any(ApplicationSubmittedEvent.class));
    }

    @Test
    @DisplayName("")
    void handleAccept_onValid_shouldAcceptAndPublish() {
        // Arrange
        var application = Application.create(case_, lawyerId, ApplicationStatus.SUBMITTED);

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));
        when(applicationRepository.save(any()))
                .thenAnswer(i->i.getArgument(0));
        when(caseRepository.save(any()))
                .thenAnswer(i->i.getArgument(0));

        var command = new AcceptApplicationCommand(1L, clientId);

        // Act
        var res = service.handle(command);

        // Assert
        assertTrue(res.isPresent());
        assertEquals(ApplicationStatus.ACCEPTED, res.get().getStatus());
        assertEquals(CaseStatus.ACCEPTED, case_.getStatus());
        verify(eventPublisher).publishEvent(any(ApplicationAcceptedEvent.class));
    }

    @Test
    @DisplayName("")
    void handleReject_onLastApplication_shouldReopenCase() {
        // Arrange
        case_.evaluation();

        var application = Application.create(case_, lawyerId, ApplicationStatus.SUBMITTED);

        when(applicationRepository.findById(1L))
                .thenReturn(Optional.of(application));
        when(applicationRepository.save(any()))
                .thenAnswer(i->i.getArgument(0));
        when(caseRepository.save(any()))
                .thenAnswer(i->i.getArgument(0));

        var command = new RejectApplicationCommand(1L, clientId);

        // Act
        var result = service.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ApplicationStatus.REJECTED, result.get().getStatus());
        assertEquals(CaseStatus.OPEN, case_.getStatus());
        verify(eventPublisher).publishEvent(any(ApplicationRejectedEvent.class));
    }
}
