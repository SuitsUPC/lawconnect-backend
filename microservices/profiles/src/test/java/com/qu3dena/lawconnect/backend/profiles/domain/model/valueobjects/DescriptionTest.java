package com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects;

import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Description} value object.
 */
class DescriptionTest {

    @Test
    void testValidDescription() {
        // Arrange
        String text = "Abogado especializado en derecho civil con 10 años de experiencia";

        // Act
        Description description = new Description(text);

        // Assert
        assertNotNull(description);
        assertEquals(text, description.text());
    }

    @Test
    void testDescriptionWithNullValue() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Description(null)
        );
        assertEquals("Description text cannot be null or blank", exception.getMessage());
    }

    @Test
    void testDescriptionWithBlankValue() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Description("   ")
        );
        assertEquals("Description text cannot be null or blank", exception.getMessage());
    }

    @Test
    void testDescriptionExceedingMaxLength() {
        // Arrange
        String longText = "A".repeat(501);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Description(longText)
        );
        assertEquals("Description text cannot exceed 500 characters", exception.getMessage());
    }

    @Test
    void testDescriptionWithExactMaxLength() {
        // Arrange
        String exactLengthText = "A".repeat(500);

        // Act
        Description description = new Description(exactLengthText);

        // Assert
        assertNotNull(description);
        assertEquals(500, description.text().length());
    }
}
