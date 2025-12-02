package com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Dni} value object.
 */
class DniTest {

    @Test
    void testValidDni() {
        // Arrange & Act
        Dni dni = new Dni("12345678");

        // Assert
        assertNotNull(dni);
        assertEquals("12345678", dni.value());
    }

    @Test
    void testDniWithNullValue() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Dni(null)
        );
        assertEquals("DNI cannot be null or blank", exception.getMessage());
    }

    @Test
    void testDniWithBlankValue() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Dni("   ")
        );
        assertEquals("DNI cannot be null or blank", exception.getMessage());
    }

    @Test
    void testDniWithInvalidFormat() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Dni("123456")
        );
        assertEquals("DNI must be exactly 8 digits (format: 12345678)", exception.getMessage());
    }

    @Test
    void testDniWithLetters() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Dni("1234567A")
        );
        assertEquals("DNI must be exactly 8 digits (format: 12345678)", exception.getMessage());
    }
}
