package com.qu3dena.lawconnect.backend.cases.domain.model.aggregates;

import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseStatus;
import com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects.CaseTitle;
import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CaseAggregateTest {

    @Test
    @DisplayName("create() should initialize case in OPEN state with one initial state entry")
    void create_shouldInitializeOpen() {
        // Arrange
        var clientId = UUID.randomUUID();
        var title = new CaseTitle("Test Case");
        var description = new Description("This is a test case description.");

        // Act
        var case_ = CaseAggregate.create(clientId, title, description);

        // Assert
        assertEquals(CaseStatus.OPEN, case_.getStatus());
        assertEquals(1, case_.getStates().size());
        assertEquals(CaseStatus.OPEN, case_.getStates().get(0).getStatus());
    }

    @Test
    @DisplayName("evaluation() should move from OPEN to EVALUATION")
    void evaluation_shouldTransitionToEvaluation() {
        // Arrange
        var clientId = UUID.randomUUID();
        var title = new CaseTitle("Test Case");
        var description = new Description("This is a test case description.");

        var case_ = CaseAggregate.create(clientId, title, description);

        // Act
        case_.evaluation();

        // Assert
        assertEquals(CaseStatus.EVALUATION, case_.getStatus());
        assertTrue(case_.getStates().stream()
                .anyMatch(s -> s.getStatus() == CaseStatus.EVALUATION));

    }

    @Test
    @DisplayName("accept() should only work when not CANCELED/CLOSED, assign lawyer and record state")
    void accept_withValidState_shouldAssignLawyer() {
        // Arrange
        var clientId = UUID.randomUUID();
        var lawyerId = UUID.randomUUID();
        var title = new CaseTitle("Test Case");
        var description = new Description("This is a test case description.");

        var case_ = CaseAggregate.create(clientId, title, description);

        // Act
        case_.accept(lawyerId);

        // Assert
        assertEquals(CaseStatus.ACCEPTED, case_.getStatus());
        assertEquals(lawyerId, case_.getAssignedLawyerId());
        assertTrue(case_.getStates().stream()
                .anyMatch(s -> s.getStatus() == CaseStatus.ACCEPTED));
    }

    @Test
    @DisplayName("cancel() only allowed from OPEN or EVALUATION")
    void cancel_onlyFromOpenOrEvaluation() {
        // Arrange
        var clientId = UUID.randomUUID();
        var title = new CaseTitle("Test Case");
        var description = new Description("This is a test case description.");

        var case_ = CaseAggregate.create(clientId, title, description);

        // Act
        case_.cancel();

        // Assert
        assertEquals(CaseStatus.CANCELED, case_.getStatus());
        assertTrue(case_.getStates().stream()
                .anyMatch(s -> s.getStatus() == CaseStatus.CANCELED));

        // Try invalid cancel
        IllegalStateException ex = assertThrows(
                IllegalStateException.class, case_::cancel);

        assertTrue(ex.getMessage().contains("Only an Open or Evaluation case can be canceled"));
    }

    @Test
    @DisplayName("close() only allowed from ACCEPTED")
    void close_onlyFromAccepted() {
        // Arrange
        var clientId = UUID.randomUUID();
        var title = new CaseTitle("Test Case");
        var description = new Description("This is a test case description.");

        var case_ = CaseAggregate.create(clientId, title, description);

        // Act & Assert: should fail on OPEN
        assertThrows(IllegalStateException.class, case_::close);

        // Move to ACCEPTED state
        case_.accept(UUID.randomUUID());
        // Now close should succeed
        case_.close();

        // Assert
        assertEquals(CaseStatus.CLOSED, case_.getStatus());
    }

    @Test
    @DisplayName("reopen() only allowed from EVALUATION")
    void reopen_onlyFromEvaluation() {
        // Arrange
        var clientId = UUID.randomUUID();
        var title = new CaseTitle("Test Case");
        var description = new Description("This is a test case description.");

        var case_ = CaseAggregate.create(clientId, title, description);

        // Act
        case_.evaluation();
        case_.reopen();

        // Assert
        assertEquals(CaseStatus.OPEN, case_.getStatus());
        assertTrue(case_.getStates().stream()
                .anyMatch(s -> s.getStatus() == CaseStatus.OPEN));

        // Try invalid reopen
        assertThrows(IllegalStateException.class, case_::reopen);
    }
}
