package com.qu3dena.lawconnect.backend.iam.domain.model.aggregates;

import com.qu3dena.lawconnect.backend.iam.domain.model.entities.Role;
import com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects.Roles;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserAggregate}.
 */
class UserAggregateTest {

    @Test
    void testCreateUserAggregateWithAllParameters() {
        // Arrange
        String username = "jperez@lawconnect.com";
        String password = "hashedPassword123";
        Role role = new Role(Roles.ROLE_LAWYER);

        // Act
        UserAggregate user = new UserAggregate(username, password, role);

        // Assert
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
        assertEquals("ROLE_LAWYER", user.getRoleName());
    }

    @Test
    void testCreateUserAggregateWithFactoryMethod() {
        // Arrange
        String username = "mgarcia@lawconnect.com";
        String password = "securePass456";
        Role role = new Role(Roles.ROLE_CLIENT);

        // Act
        UserAggregate user = UserAggregate.create(username, password, role);

        // Assert
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
        assertEquals("ROLE_CLIENT", user.getRoleName());
    }

    @Test
    void testDefaultConstructorAssignsDefaultRole() {
        // Act
        UserAggregate user = new UserAggregate();

        // Assert
        assertNotNull(user);
        assertNotNull(user.getRole());
        assertEquals(Roles.ROLE_CLIENT, user.getRole().getName());
        assertEquals("ROLE_CLIENT", user.getRoleName());
    }

    @Test
    void testGetRoleName() {
        // Arrange
        String username = "lawyer@lawconnect.com";
        String password = "pass789";
        Role lawyerRole = new Role(Roles.ROLE_LAWYER);
        UserAggregate user = new UserAggregate(username, password, lawyerRole);

        // Act
        String roleName = user.getRoleName();

        // Assert
        assertEquals("ROLE_LAWYER", roleName);
    }

    @Test
    void testUserAggregateSetters() {
        // Arrange
        UserAggregate user = new UserAggregate();

        // Act
        user.setUsername("newuser@lawconnect.com");
        user.setPassword("newHashedPassword");
        Role newRole = new Role(Roles.ROLE_LAWYER);
        user.setRole(newRole);

        // Assert
        assertEquals("newuser@lawconnect.com", user.getUsername());
        assertEquals("newHashedPassword", user.getPassword());
        assertEquals(newRole, user.getRole());
    }
}
