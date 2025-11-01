package com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/**
 * Value object representing a person's full name.
 * <p>
 * Ensures that both firstname and lastname are non-null and non-blank.
 * </p>
 *
 * @param firstname the person's first name, must not be null or blank
 * @param lastname  the person's last name, must not be null or blank
 */
@Embeddable
public record FullName(String firstname, String lastname) {

    /**
     * Constructs a {@code FullName} value object.
     *
     * @param firstname the person's first name
     * @param lastname  the person's last name
     * @throws IllegalArgumentException if either firstname or lastname is null or blank
     */
    public FullName {
        if (firstname == null || firstname.isBlank())
            throw new IllegalArgumentException("Firstname cannot be null or blank");

        if (lastname == null || lastname.isBlank())
            throw new IllegalArgumentException("Lastname cannot be null or blank");
    }

    /**
     * Returns the full name as a single string in the format "firstname lastname".
     *
     * @return the concatenated full name
     */
    @Override
    public String toString() {
        return firstname + " " + lastname;
    }
}
