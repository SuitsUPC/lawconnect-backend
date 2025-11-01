package com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates;

import com.qu3dena.lawconnect.backend.profiles.domain.model.entities.LawyerSpecialty;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.ContactInfo;
import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.Dni;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.FullName;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate root representing a lawyer profile in the system.
 */
@Data
@Entity
@Table(name = "lawyer_profiles")
@EqualsAndHashCode(callSuper = true)
public class LawyerAggregate extends ProfileAggregate {

    /**
     * The description of the lawyer.
     */
    @Embedded
    @AttributeOverride(
            name = "text",
            column = @Column(name = "description", length = 500, nullable = false)
    )
    private Description description;

    /**
     * The set of specialties associated with the lawyer.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "lawyer_profile_specialties",
            joinColumns = @JoinColumn(name = "lawyer_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private Set<LawyerSpecialty> specialties;

    /*TODO: Add list of certificates List<Certificate>*/

    /**
     * Default constructor initializing specialties as an empty set.
     */
    public LawyerAggregate() {
        this.specialties = new HashSet<>();
    }

    /**
     * Constructs a new LawyerProfileAggregate with the specified parameters.
     *
     * @param userId      the unique identifier of the user
     * @param fullName    the full name of the lawyer
     * @param contact     the contact information of the lawyer
     * @param dni         the DNI (National Identity Document) of the lawyer
     * @param description the description of the lawyer
     * @param specialties the set of specialties of the lawyer
     */
    public LawyerAggregate(
            UUID userId,
            FullName fullName,
            ContactInfo contact,
            Dni dni,
            Description description,
            Set<LawyerSpecialty> specialties
    ) {
        super(userId, fullName, contact, dni);
        this.description = description;
        this.specialties = specialties != null ? specialties : new HashSet<>();
    }

    /**
     * Creates a new instance of LawyerProfileAggregate with the given parameters.
     *
     * @param userId   the unique identifier of the user
     * @param fullName the full name of the lawyer
     * @param contact  the contact information of the lawyer
     * @param dni      the DNI (National Identity Document) of the lawyer
     * @return a new LawyerProfileAggregate instance
     */
    public static LawyerAggregate create(
            UUID userId,
            FullName fullName,
            ContactInfo contact,
            Dni dni,
            Description description,
            Set<LawyerSpecialty> specialties
    ) {
        return new LawyerAggregate(userId, fullName, contact, dni, description, specialties);
    }

    public String getDescriptionText() {
        return description.text();
    }
}
