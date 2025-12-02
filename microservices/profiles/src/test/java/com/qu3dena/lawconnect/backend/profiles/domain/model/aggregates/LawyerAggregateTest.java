package com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates;

import com.qu3dena.lawconnect.backend.profiles.domain.model.entities.LawyerSpecialty;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.Dni;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.FullName;
import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LawyerAggregate}.
 */
class LawyerAggregateTest {

    @Test
    void testCreateLawyerAggregateWithAllParameters() {
        // Arrange
        UUID userId = UUID.randomUUID();
        FullName fullName = new FullName("Carlos", "Rodríguez");
        ContactInfo contactInfo = new ContactInfo("987654321", "Av. Arequipa 1234");
        Dni dni = new Dni("12345678");
        Description description = new Description("Abogado especializado en derecho civil");
        Set<LawyerSpecialty> specialties = new HashSet<>();

        // Act
        LawyerAggregate lawyer = new LawyerAggregate(userId, fullName, contactInfo, dni, description, specialties);

        // Assert
        assertNotNull(lawyer);
        assertEquals(userId, lawyer.getUserId());
        assertEquals(fullName, lawyer.getFullName());
        assertEquals(contactInfo, lawyer.getContact());
        assertEquals(dni, lawyer.getDni());
        assertEquals(description, lawyer.getDescription());
        assertNotNull(lawyer.getSpecialties());
    }

    @Test
    void testCreateLawyerAggregateWithFactoryMethod() {
        // Arrange
        UUID userId = UUID.randomUUID();
        FullName fullName = new FullName("María", "López");
        ContactInfo contactInfo = new ContactInfo("912345678", "Jr. Huancayo 567");
        Dni dni = new Dni("87654321");
        Description description = new Description("Especialista en derecho laboral");
        Set<LawyerSpecialty> specialties = new HashSet<>();

        // Act
        LawyerAggregate lawyer = LawyerAggregate.create(userId, fullName, contactInfo, dni, description, specialties);

        // Assert
        assertNotNull(lawyer);
        assertEquals(userId, lawyer.getUserId());
        assertEquals(fullName, lawyer.getFullName());
        assertEquals(contactInfo, lawyer.getContact());
        assertEquals(dni, lawyer.getDni());
        assertEquals("Especialista en derecho laboral", lawyer.getDescriptionText());
    }

    @Test
    void testDefaultConstructorInitializesEmptySpecialties() {
        // Act
        LawyerAggregate lawyer = new LawyerAggregate();

        // Assert
        assertNotNull(lawyer);
        assertNotNull(lawyer.getSpecialties());
        assertTrue(lawyer.getSpecialties().isEmpty());
    }

    @Test
    void testCreateLawyerAggregateWithNullSpecialties() {
        // Arrange
        UUID userId = UUID.randomUUID();
        FullName fullName = new FullName("Pedro", "Sánchez");
        ContactInfo contactInfo = new ContactInfo("923456789", "Av. La Marina 890");
        Dni dni = new Dni("11223344");
        Description description = new Description("Abogado penalista con experiencia");

        // Act
        LawyerAggregate lawyer = LawyerAggregate.create(userId, fullName, contactInfo, dni, description, null);

        // Assert
        assertNotNull(lawyer);
        assertNotNull(lawyer.getSpecialties());
        assertTrue(lawyer.getSpecialties().isEmpty());
    }

    @Test
    void testGetDescriptionText() {
        // Arrange
        UUID userId = UUID.randomUUID();
        FullName fullName = new FullName("Ana", "Torres");
        ContactInfo contactInfo = new ContactInfo("934567890", "Calle Lima 123");
        Dni dni = new Dni("55667788");
        Description description = new Description("Especializada en derecho corporativo");
        
        LawyerAggregate lawyer = LawyerAggregate.create(
                userId, fullName, contactInfo, dni, description, new HashSet<>()
        );

        // Act
        String descriptionText = lawyer.getDescriptionText();

        // Assert
        assertEquals("Especializada en derecho corporativo", descriptionText);
    }
}
