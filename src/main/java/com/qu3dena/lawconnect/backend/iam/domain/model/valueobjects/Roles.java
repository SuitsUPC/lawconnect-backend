package com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects;

/**
 * Enumeration of user roles within the LawConnect system.
 * <p>
 * Defines the available roles for access control and authorization.
 *
 * <ul>
 *   <li>{@link #ROLE_ADMIN} - Administrator with full system access.</li>
 *   <li>{@link #ROLE_LAWYER} - Lawyer with permissions to manage legal cases.</li>
 *   <li>{@link #ROLE_CLIENT} - Client with access to their own cases and information.</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum Roles {
    /** Administrator with full system access. */
    ROLE_ADMIN,
    /** Lawyer with permissions to manage legal cases. */
    ROLE_LAWYER,
    /** Client with access to their own cases and information. */
    ROLE_CLIENT
}
