package com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects;

/**
 * Enumeration representing the status of an invitation.
 * <p>
 * This enum defines the possible states an invitation can have in the system.
 * It is used to track and manage the progress of an invitation.
 *
 * <p>Possible statuses include:</p>
 * <ul>
 *   <li>{@link #PENDING} - The invitation has been sent and is awaiting a response.</li>
 *   <li>{@link #ACCEPTED} - The invitation has been accepted.</li>
 *   <li>{@link #REJECTED} - The invitation has been rejected.</li>
 * </ul>
 *
 * <p>Use this enum to represent and handle invitation statuses in a consistent manner.</p>
 *
 * @author LawConnect Team
 * @since 1.0
 */
public enum InvitationStatus {
    /**
     * The invitation has been sent and is awaiting a response.
     */
    PENDING,

    /**
     * The invitation has been accepted.
     */
    ACCEPTED,

    /**
     * The invitation has been rejected.
     */
    REJECTED,
}
