package com.qu3dena.lawconnect.backend.cases.application.internal.queryservices;

import com.qu3dena.lawconnect.backend.cases.domain.model.aggregates.CaseAggregate;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Application;
import com.qu3dena.lawconnect.backend.cases.domain.model.entities.Invitation;
import com.qu3dena.lawconnect.backend.cases.domain.model.queries.*;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseTitle;
import com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories.*;
import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CaseQueryServiceImplTest {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private CaseQueryServiceImpl service;

    UUID clientId;
    UUID lawyerId;
    CaseAggregate case_;

    @BeforeEach
    void init() {
        clientId = UUID.randomUUID();
        lawyerId = UUID.randomUUID();
        
        case_ = CaseAggregate.create(
                clientId,
                new CaseTitle("Test Case"),
                new Description("This is a test case description.")
        );
    }

    @Test
    @DisplayName("handle GetCasesByClientIdQuery returns empty list when no cases exist")
    void handleGetByClientId_shouldReturnEmptyList() {
        // Arrange
        var clientId = UUID.randomUUID();
        when(caseRepository.findByClientId(clientId)).thenReturn(List.of());
        var query = new GetCasesByClientIdQuery(clientId);

        // Act
        var result = service.handle(query);

        // Assert
        assertNotNull(result, "Expected a non-null result from the query handler");
        assertTrue(result.isEmpty(), "Expected no cases for a client without cases");
        verify(caseRepository).findByClientId(clientId);
    }

    @Test
    @DisplayName("handle GetCasesByClientIdQuery returns list when cases exist")
    void handleGetByClientId_shouldReturnListWhenExists() {
        // Arrange
        when(caseRepository.findByClientId(clientId))
                .thenReturn(List.of(case_));
        var query = new GetCasesByClientIdQuery(clientId);

        // Act
        var result = service.handle(query);

        // Assert
        assertEquals(1, result.size(), "Expected one case for the client");
        assertEquals(case_, result.get(0));
        verify(caseRepository).findByClientId(clientId);
    }

    @Test
    @DisplayName("handle GetCasesByLawyerIdQuery returns empty list when none accepted")
    void handleGetCasesByLawyerId_shouldReturnEmptyList() {
        // Arrange
        when(caseRepository.findByAssignedLawyerIdAndCurrentStatus(lawyerId, CaseStatus.ACCEPTED))
                .thenReturn(List.of());
        var query = new GetCasesByLawyerIdQuery(lawyerId);

        // Act
        var result = service.handle(query);

        // Assert
        assertTrue(result.isEmpty(), "Expected no accepted cases for the lawyer");
        verify(caseRepository).findByAssignedLawyerIdAndCurrentStatus(lawyerId, CaseStatus.ACCEPTED);
    }

    @Test
    @DisplayName("handle GetSuggestedCasesQuery excludes invited cases")
    void handleGetSuggestedCasesByLawyerId_shouldExcludeInvitedCases() {
        // Arrange
        when(caseRepository.findByCurrentStatus(CaseStatus.OPEN))
                .thenReturn(List.of(case_));
        when(applicationRepository.findByLawyerId(lawyerId))
                .thenReturn(List.of());

        var invitation = mock(Invitation.class);

        when(invitation.getLegalCase())
                .thenReturn(case_);
        when(invitationRepository.findByLawyerId(lawyerId))
                .thenReturn(List.of(invitation));

        var query = new GetSuggestedCasesQuery(lawyerId);

        // Act
        var result = service.handle(query);

        // Assert
        assertTrue(result.isEmpty(), "Invited case should be filtered out");
    }

   @Test
   @DisplayName("handle GetSuggestedCasesQuery excludes applied cases")
   void handleGetSuggestedCasesByLawyerId_shouldExcludeAppliedCases() {
       // Arrange
       when(caseRepository.findByCurrentStatus(CaseStatus.OPEN))
               .thenReturn(List.of(case_));
       when(invitationRepository.findByLawyerId(lawyerId))
               .thenReturn(List.of());

       var application = mock(Application.class);

       when(application.getLegalCase())
               .thenReturn(case_);
       when(applicationRepository.findByLawyerId(lawyerId))
               .thenReturn(List.of(application));

       var query = new GetSuggestedCasesQuery(lawyerId);

       // Act
       var result = service.handle(query);

       // Assert
       assertTrue(result.isEmpty(), "Applied case should be filtered out");
   }

    @Test
    @DisplayName("handle GetCaseByIdQuery returns case when exists")
    void handleGetCaseById_exists_shouldReturnCase() {
        // Arrange
        var caseId = case_.getId();
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(case_));
        var query = new GetCaseByIdQuery(caseId);

        // Act
        var result = service.handle(query);

        // Assert
        assertTrue(result.isPresent(), "Expected a case to be found for the given ID");
        assertEquals(case_, result.get(), "Expected the returned case to match the mock");
        verify(caseRepository).findById(caseId);
    }

    @Test
    @DisplayName("handle GetCaseByIdQuery returns empty when case does not exist")
    void handleGetCaseById_doesNotExist_shouldReturnEmpty() {
        // Arrange
        var nonExistentId = UUID.randomUUID();
        when(caseRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        var query = new GetCaseByIdQuery(nonExistentId);

        // Act
        var result = service.handle(query);

        // Assert
        assertTrue(result.isEmpty(), "Expected no case for a non-existent ID");
        verify(caseRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("handle GetCasesByStatusQuery returns filtered list by status")
    void handleGetCasesByStatus_shouldReturnFilteredList() {
        // Arrange
        var openList = List.of(mock(CaseAggregate.class));
        when(caseRepository.findByCurrentStatus(CaseStatus.OPEN)).thenReturn(openList);
        var query = new GetCasesByStatusQuery(CaseStatus.OPEN);

        // Act
        var result = service.handle(query);

        // Assert
        assertEquals(openList, result, "Expected only cases with OPEN status");
        verify(caseRepository).findByCurrentStatus(CaseStatus.OPEN);
    }

    @Test
    @DisplayName("handle GetCasesByLawyerIdQuery returns accepted cases for lawyer")
    void handleGetCasesByLawyerId_shouldReturnAcceptedCases() {
        // Arrange
        var acceptedList = List.of(mock(CaseAggregate.class));
        when(caseRepository.findByAssignedLawyerIdAndCurrentStatus(lawyerId, CaseStatus.ACCEPTED))
                .thenReturn(acceptedList);
        var query = new GetCasesByLawyerIdQuery(lawyerId);

        // Act
        var result = service.handle(query);

        // Assert
        assertEquals(acceptedList, result, "Expected accepted cases for the given lawyer ID");
        verify(caseRepository).findByAssignedLawyerIdAndCurrentStatus(lawyerId, CaseStatus.ACCEPTED);
    }

    @Test
    @DisplayName("handle GetSuggestedCasesQuery excludes invited and applied cases")
    void handleGetSuggestedCasesByLawyerId_shouldReturnListOfSuggestedCasesExcludingInvitedAndApplied() {
        // Arrange
        var caseId = case_.getId();
        var openMock = mock(CaseAggregate.class);
        when(openMock.getId()).thenReturn(caseId);

        var openCases = List.of(openMock);
        when(caseRepository.findByCurrentStatus(CaseStatus.OPEN)).thenReturn(openCases);
        when(invitationRepository.findByLawyerId(lawyerId)).thenReturn(List.of());
        when(applicationRepository.findByLawyerId(lawyerId)).thenReturn(List.of());

        var query = new GetSuggestedCasesQuery(lawyerId);

        // Act
        var result = service.handle(query);

        // Assert
        assertEquals(openCases, result, "Expected open cases when no invitations or applications exist");
        verify(caseRepository).findByCurrentStatus(CaseStatus.OPEN);
        verify(invitationRepository).findByLawyerId(lawyerId);
        verify(applicationRepository).findByLawyerId(lawyerId);
    }
}
