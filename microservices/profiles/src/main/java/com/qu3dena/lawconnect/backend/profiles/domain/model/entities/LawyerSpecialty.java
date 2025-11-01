package com.qu3dena.lawconnect.backend.profiles.domain.model.entities;

import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.LawyerSpecialties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entity representing a lawyer's specialty.
 * <p>
 * Each instance corresponds to a specific legal specialty that a lawyer may have.
 * </p>
 */
@Entity
@NoArgsConstructor
@Table(name = "lawyer_specialties")
public class LawyerSpecialty {

    /**
     * Unique identifier for the lawyer specialty.
     */
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the specialty, represented as an enum.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 30, unique = true, nullable = false)
    private LawyerSpecialties name;

    /**
     * Constructs a {@code LawyerSpecialty} with the given specialty name.
     *
     * @param name the specialty as a {@link LawyerSpecialties} enum
     */
    public LawyerSpecialty(LawyerSpecialties name) {
        this.name = name;
    }

    /**
     * Creates a {@code LawyerSpecialty} from a string name.
     *
     * @param name the name of the specialty as a string
     * @return a new {@code LawyerSpecialty} instance
     */
    public static LawyerSpecialty toLawyerSpecialtyFromName(String name) {
        return new LawyerSpecialty(LawyerSpecialties.valueOf(name));
    }

    /**
     * Validates and converts a set of specialty names to a set of {@code LawyerSpecialty} entities.
     *
     * @param specialties set of specialty names as strings
     * @return set of {@code LawyerSpecialty} entities
     */
    public static Set<LawyerSpecialty> validateLawyerSpecialties(Set<String> specialties) {
        return specialties.stream()
                .map(LawyerSpecialty::toLawyerSpecialtyFromName)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the name of the specialty as a string.
     *
     * @return the specialty name
     */
    public String getStringName() {
        return this.name.name();
    }

    /**
     * Factory method to create a {@code LawyerSpecialty} from a string name.
     *
     * @param name the specialty name as a string
     * @return a new {@code LawyerSpecialty} instance
     */
    public static LawyerSpecialty create(String name) {
        return toLawyerSpecialtyFromName(name);
    }
}
