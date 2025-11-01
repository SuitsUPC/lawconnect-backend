package com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.Dni;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.FullName;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Aggregate root representing a client profile in the system.
 */
@Entity
@NoArgsConstructor
@Table(name = "client_profiles")
public class ClientAggregate extends ProfileAggregate {

    /**
     * Constructs a new ClientProfileAggregate with the specified user ID, full name, and contact information.
     *
     * @param userId   the unique identifier of the user
     * @param fullName the full name of the client
     * @param contact  the contact information of the client
     * @param dni      the DNI (National Identity Document) of the client
     */
    public ClientAggregate(UUID userId, FullName fullName, ContactInfo contact, Dni dni) {
        super(userId, fullName, contact, dni);
    }

    /**
     * Static factory method to create a new {@link ClientAggregate} instance.
     *
     * @param userId   the unique identifier of the user
     * @param fullName the full name of the user
     * @param contact  the contact information of the user
     * @param dni      the national identification number of the user
     * @return a new instance of {@link ClientAggregate}
     */
    public static ClientAggregate create(UUID userId, FullName fullName, ContactInfo contact, Dni dni) {
        return new ClientAggregate(userId, fullName, contact, dni);
    }
}
