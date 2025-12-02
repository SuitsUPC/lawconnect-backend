package com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FullName} value object.
 */
class FullNameTest {

    @Test
    void testValidFullName() {
        // Arrange & Act
        FullName fullName = new FullName("Juan", "Pérez");

        // Assert
        assertNotNull(fullName);
        assertEquals("Juan", fullName.firstname());
        assertEquals("Pérez", fullName.lastname());
        assertEquals("Juan Pérez", fullName.toString());
    }

    @Test
    void testFullNameWithNullFirstname() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new FullName(null, "Pérez")
        );
        assertEquals("Firstname cannot be null or blank", exception.getMessage());
    }

    @Test
    void testFullNameWithBlankFirstname() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new FullName("   ", "Pérez")
        );
        assertEquals("Firstname cannot be null or blank", exception.getMessage());
    }

    @Test
    void testFullNameWithNullLastname() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new FullName("Juan", null)
        );
        assertEquals("Lastname cannot be null or blank", exception.getMessage());
    }

    @Test
    void testFullNameWithBlankLastname() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new FullName("Juan", "   ")
        );
        assertEquals("Lastname cannot be null or blank", exception.getMessage());
    }

    @Test
    void testFullNameToString() {
        // Arrange
        FullName fullName = new FullName("María", "García");

        // Act
        String result = fullName.toString();

        // Assert
        assertEquals("María García", result);
    }
}
