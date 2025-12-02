package com.qu3dena.lawconnect.backend.iam.domain.model.entities;

import com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects.Roles;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Role} entity.
 */
class RoleTest {

    @Test
    void testCreateRoleWithName() {
        // Arrange
        Roles roleName = Roles.ROLE_LAWYER;

        // Act
        Role role = new Role(roleName);

        // Assert
        assertNotNull(role);
        assertEquals(Roles.ROLE_LAWYER, role.getName());
        assertEquals("ROLE_LAWYER", role.getStringName());
    }

    @Test
    void testGetDefaultRole() {
        // Act
        Role defaultRole = Role.getDefaultRole();

        // Assert
        assertNotNull(defaultRole);
        assertEquals(Roles.ROLE_CLIENT, defaultRole.getName());
        assertEquals("ROLE_CLIENT", defaultRole.getStringName());
    }

    @Test
    void testToRoleFromNameWithPrefix() {
        // Act
        Role role = Role.toRoleFromName("ROLE_LAWYER");

        // Assert
        assertNotNull(role);
        assertEquals(Roles.ROLE_LAWYER, role.getName());
    }

    @Test
    void testToRoleFromNameWithoutPrefix() {
        // Act
        Role role = Role.toRoleFromName("CLIENT");

        // Assert
        assertNotNull(role);
        assertEquals(Roles.ROLE_CLIENT, role.getName());
    }

    @Test
    void testCreateRoleFromString() {
        // Act
        Role role = Role.create("LAWYER");

        // Assert
        assertNotNull(role);
        assertEquals(Roles.ROLE_LAWYER, role.getName());
    }

    @Test
    void testValidateRolesWithEmptySet() {
        // Arrange
        Set<Role> emptySet = new HashSet<>();

        // Act
        Set<Role> result = Role.validateRoles(emptySet);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getName() == Roles.ROLE_CLIENT));
    }

    @Test
    void testValidateRolesWithNullSet() {
        // Act
        Set<Role> result = Role.validateRoles(null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getName() == Roles.ROLE_CLIENT));
    }

    @Test
    void testValidateRolesWithValidSet() {
        // Arrange
        Set<Role> validSet = Set.of(new Role(Roles.ROLE_LAWYER));

        // Act
        Set<Role> result = Role.validateRoles(validSet);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getName() == Roles.ROLE_LAWYER));
    }
}
