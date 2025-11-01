package com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects;

/**
 * Enumeration representing the status of an application.
 * <p>
 * This enum defines the possible states an application can have in the system.
 * It is used to track and manage the progress of an application.
 *
 * <p>Possible statuses include:</p>
 * <ul>
 *   <li>{@link #SUBMITTED} - The application has been submitted and is awaiting review.</li>
 *   <li>{@link #ACCEPTED} - The application has been reviewed and accepted.</li>
 *   <li>{@link #REJECTED} - The application has been reviewed and rejected.</li>
 * </ul>
 *
 * <p>Use this enum to represent and handle application statuses in a consistent manner.</p>
 *
 * @author LawConnect Team
 * @since 1.0
 */
public enum ApplicationStatus {
    /**
     * The application has been submitted and is awaiting review.
     */
    SUBMITTED,

    /**
     * The application has been reviewed and accepted.
     */
    ACCEPTED,

    /**
     * The application has been reviewed and rejected.
     */
    REJECTED,
}
