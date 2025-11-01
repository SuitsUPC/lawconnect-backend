package com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects;

/**
 * Represents the type of a comment in the system.
 * <p>
 * This enum defines the possible types of comments that can be associated
 * with a case, such as general comments or those related to the final review.
 * </p>
 *
 * <ul>
 *     <li>GENERAL: A general comment not tied to a specific phase.</li>
 *     <li>FINAL_REVIEW: A comment specifically for the final review phase.</li>
 * </ul>
 *
 * @author GonzaloQuedena
 * @since 1.0.0
 */
public enum CommentType {
    /**
     * A general comment not tied to a specific phase.
     */
    GENERAL,

    /**
     * A comment specifically for the final review phase.
     */
    FINAL_REVIEW
}
