package com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects;

/**
 * Enumeration representing the status of a case.
 * <p>
 * This enum defines the possible states a case can be in during its lifecycle.
 * It is used to track and manage the progress of a case in the system.
 *
 * <p>Possible statuses include:</p>
 * <ul>
 *   <li>{@link #OPEN} - The case is newly created and open for processing.</li>
 *   <li>{@link #EVALUATION} - The case is under evaluation.</li>
 *   <li>{@link #ACCEPTED} - The case has been accepted for further action.</li>
 *   <li>{@link #CLOSED} - The case has been resolved and closed.</li>
 *   <li>{@link #CANCELED} - The case has been cancelled.</li>
 * </ul>
 *
 * @author GonzaloQu\
 * @since 1.0
 */
public enum CaseStatus {
    /**
     * The case is newly created and open for processing.
     */
    OPEN,

    /**
     * The case is under evaluation.
     */
    EVALUATION,

    /**
     * The case has been accepted for further action.
     */
    ACCEPTED,

    /**
     * The case has been resolved and closed.
     */
    CLOSED,

    /**
     * The case has been cancelled.
     */
    CANCELED,
}
