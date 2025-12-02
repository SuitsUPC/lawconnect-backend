package com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.Dni;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.FullName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ClientAggregate}.
 */
class ClientAggregateTest {

    @Test
    void testCreateClientAggregateWithConstructor() {
        // Arrange
        UUID userId = UUID.randomUUID();
        FullName fullName = new FullName("Luis", "Fernández");
        ContactInfo contactInfo = new ContactInfo("945678901", "Av. Brasil 456");
        Dni dni = new Dni("99887766");

        // Act
        ClientAggregate client = new ClientAggregate(userId, fullName, contactInfo, dni);

        // Assert
        assertNotNull(client);
        assertEquals(userId, client.getUserId());
        assertEquals(fullName, client.getFullName());
        assertEquals(contactInfo, client.getContact());
        assertEquals(dni, client.getDni());
    }

    @Test
    void testCreateClientAggregateWithFactoryMethod() {
        // Arrange
        UUID userId = UUID.randomUUID();
        FullName fullName = new FullName("Carmen", "Vega");
        ContactInfo contactInfo = new ContactInfo("956789012", "Jr. Puno 789");
        Dni dni = new Dni("44332211");

        // Act
        ClientAggregate client = ClientAggregate.create(userId, fullName, contactInfo, dni);

        // Assert
        assertNotNull(client);
        assertEquals(userId, client.getUserId());
        assertEquals(fullName, client.getFullName());
        assertEquals(contactInfo, client.getContact());
        assertEquals(dni, client.getDni());
    }

    @Test
    void testDefaultConstructor() {
        // Act
        ClientAggregate client = new ClientAggregate();

        // Assert
        assertNotNull(client);
    }

    @Test
    void testClientAggregateInheritsFromProfileAggregate() {
        // Arrange
        UUID userId = UUID.randomUUID();
        FullName fullName = new FullName("Roberto", "Méndez");
        ContactInfo contactInfo = new ContactInfo("967890123", "Calle Cusco 321");
        Dni dni = new Dni("22334455");

        // Act
        ClientAggregate client = ClientAggregate.create(userId, fullName, contactInfo, dni);

        // Assert
        assertTrue(client instanceof ProfileAggregate);
    }

    @Test
    void testClientAggregateWithCompleteData() {
        // Arrange
        UUID userId = UUID.randomUUID();
        FullName fullName = new FullName("Patricia", "Gutiérrez");
        ContactInfo contactInfo = new ContactInfo("978901234", "Av. Universitaria 999");
        Dni dni = new Dni("66778899");

        // Act
        ClientAggregate client = ClientAggregate.create(userId, fullName, contactInfo, dni);

        // Assert
        assertNotNull(client);
        assertEquals("Patricia", client.getFullName().firstname());
        assertEquals("Gutiérrez", client.getFullName().lastname());
        assertEquals("978901234", client.getContact().phoneNumber());
        assertEquals("Av. Universitaria 999", client.getContact().address());
        assertEquals("66778899", client.getDni().value());
    }
}
