package com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/**
 * Value object representing a person's contact information.
 * <p>
 * Ensures that both phone number and address are non-null and non-blank.
 * </p>
 *
 * @param phoneNumber the person's phone number, must not be null or blank
 * @param address     the person's address, must not be null or blank
 */
@Embeddable
public record ContactInfo(String phoneNumber, String address) {

    /**
     * Constructs a {@code ContactInfo} value object.
     *
     * @param phoneNumber the person's phone number
     * @param address     the person's address
     * @throws IllegalArgumentException if either phoneNumber or address is null or blank
     */
    public ContactInfo {
        if (phoneNumber == null || phoneNumber.isBlank())
            throw new IllegalArgumentException("Phone number cannot be null or blank");

        if (address == null || address.isBlank())
            throw new IllegalArgumentException("Address cannot be null or blank");
    }
}
