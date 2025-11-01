package com.qu3dena.lawconnect.backend.cases.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/**
 * Value object representing the title of a case.
 * <p>
 * This class is immutable and ensures that the title text is not null or empty.
 * It is annotated with {@link Embeddable} to indicate that it can be embedded
 * in a JPA entity.
 *
 * <p>Use this value object to encapsulate the logic for validating and handling
 * case titles.</p>
 *
 * @author LawConnect Team
 * @since 1.0
 */
@Embeddable
public record CaseTitle(String text) {
    /**
     * Constructs a new {@code CaseTitle} instance.
     *
     * @param text the text of the case title
     */
    public CaseTitle {
        if (text == null || text.isEmpty())
            throw new IllegalArgumentException("text cannot be null or empty");
    }
}
