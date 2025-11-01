package com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.Dni;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.FullName;
import com.qu3dena.lawconnect.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Abstract aggregate root representing a user profile in the system.
 * Serves as a base for specific profile types such as client and lawyer.
 */
@Data
@MappedSuperclass
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class ProfileAggregate extends AuditableAbstractAggregateRoot<ProfileAggregate> {

    /**
     * Unique identifier for the user.
     */
    @Column(unique = true, nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    /**
     * Full name of the user.
     */
    @Embedded
    private FullName fullName;

    /**
     * Contact information of the user.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "phoneNumber", column = @Column(name = "contact_phone_number", nullable = false)),
            @AttributeOverride(name = "address", column = @Column(name = "contact_address", nullable = false))
    })
    private ContactInfo contact;

    /**
     * National identification number (DNI) of the user.
     */
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "dni", nullable = false))
    private Dni dni;

    /**
     * Constructs a new ProfileAggregate with the specified user ID, full name, and contact information.
     *
     * @param userId   the unique identifier of the user
     * @param fullName the full name of the user
     * @param contact  the contact information of the user
     * @param dni      the national identification number of the user
     */
    public ProfileAggregate(UUID userId, FullName fullName, ContactInfo contact, Dni dni) {
        this.userId = userId;
        this.fullName = fullName;
        this.contact = contact;
        this.dni = dni;
    }

    /**
     * Updates the full name of the user.
     *
     * @param fullName the new full name
     */
    void updateFullName(FullName fullName) {
        this.fullName = fullName;
    }

    /**
     * Updates the contact information of the user.
     *
     * @param contactInfo the new contact information
     */
    void updateContactInfo(ContactInfo contactInfo) {
        this.contact = contactInfo;
    }

    /**
     * Retrieves the value of the user's national identification number (DNI).
     *
     * @return the DNI value as a String
     */
    public String getDniValue () {
        return this.dni.value();
    }
}
