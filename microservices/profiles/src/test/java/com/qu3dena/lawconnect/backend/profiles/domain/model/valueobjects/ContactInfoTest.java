package com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ContactInfo} value object.
 */
class ContactInfoTest {

    @Test
    void testValidContactInfo() {
        // Arrange & Act
        ContactInfo contactInfo = new ContactInfo("987654321", "Av. Los Olivos 123");

        // Assert
        assertNotNull(contactInfo);
        assertEquals("987654321", contactInfo.phoneNumber());
        assertEquals("Av. Los Olivos 123", contactInfo.address());
    }

    @Test
    void testContactInfoWithNullPhoneNumber() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ContactInfo(null, "Av. Los Olivos 123")
        );
        assertEquals("Phone number cannot be null or blank", exception.getMessage());
    }

    @Test
    void testContactInfoWithBlankPhoneNumber() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ContactInfo("   ", "Av. Los Olivos 123")
        );
        assertEquals("Phone number cannot be null or blank", exception.getMessage());
    }

    @Test
    void testContactInfoWithNullAddress() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ContactInfo("987654321", null)
        );
        assertEquals("Address cannot be null or blank", exception.getMessage());
    }

    @Test
    void testContactInfoWithBlankAddress() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ContactInfo("987654321", "   ")
        );
        assertEquals("Address cannot be null or blank", exception.getMessage());
    }
}
