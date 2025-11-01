package com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/**
 * Value object representing a textual description.
 * <p>
 * Ensures that the description text is non-null, non-blank, and does not exceed 500 characters.
 * </p>
 *
 * @param text the description text, must not be null, blank, or longer than 500 characters
 */
@Embeddable
public record Description(String text) {

    /**
     * Constructs a {@code Description} value object.
     *
     * @param text the description text
     */
    public Description {
        if (text == null || text.isBlank())
            throw new IllegalArgumentException("Description text cannot be null or blank");

        if (text.length() > 500)
            throw new IllegalArgumentException("Description text cannot exceed 500 characters");
    }
}
