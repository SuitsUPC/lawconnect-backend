package com.qu3dena.lawconnect.backend.iam.application.internal.outboundservices.profiles;

import com.qu3dena.lawconnect.backend.iam.domain.model.aggregates.UserAggregate;
import com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Outbound service responsible for provisioning a basic profile in the Profiles microservice
 * each time a new IAM user is created.
 */
@Service
public class ProfileProvisioningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileProvisioningService.class);

    private static final String DEFAULT_PHONE_NUMBER = "+000000000";
    private static final String DEFAULT_ADDRESS = "Completar direccion";
    private static final String DEFAULT_DESCRIPTION = "Perfil en construccion";
    private static final String DEFAULT_LASTNAME = "LawConnect";

    private final RestClient profilesRestClient;

    public ProfileProvisioningService(RestClient profilesRestClient) {
        this.profilesRestClient = profilesRestClient;
    }

    /**
     * Creates a placeholder profile in the Profiles service for the provided {@link UserAggregate}.
     * If the remote call fails, the exception is logged but the sign-up flow is not interrupted.
     *
     * @param userAggregate the recently created user
     */
    public void provisionProfileFor(UserAggregate userAggregate) {
        try {
            if (userAggregate.getRole().getName() == Roles.ROLE_LAWYER) {
                createLawyerProfile(userAggregate);
            } else if (userAggregate.getRole().getName() == Roles.ROLE_CLIENT) {
                createClientProfile(userAggregate);
            } else {
                LOGGER.info("Skipping profile provisioning for user {} because role {} has no profile.",
                        userAggregate.getId(), userAggregate.getRoleName());
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to provision profile for user {}: {}", userAggregate.getId(), exception.getMessage());
        }
    }

    private void createClientProfile(UserAggregate userAggregate) {
        var payload = new CreateClientPayload(
                userAggregate.getId(),
                deriveFirstname(userAggregate.getUsername()),
                deriveLastname(userAggregate.getUsername()),
                generatePlaceholderDni(userAggregate.getId()),
                new ContactInfoPayload(DEFAULT_PHONE_NUMBER, DEFAULT_ADDRESS)
        );

        profilesRestClient.post()
                .uri("/api/v1/clients")
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    private void createLawyerProfile(UserAggregate userAggregate) {
        var payload = new CreateLawyerPayload(
                userAggregate.getId(),
                deriveFirstname(userAggregate.getUsername()),
                deriveLastname(userAggregate.getUsername()),
                generatePlaceholderDni(userAggregate.getId()),
                new ContactInfoPayload(DEFAULT_PHONE_NUMBER, DEFAULT_ADDRESS),
                DEFAULT_DESCRIPTION,
                Set.of()
        );

        profilesRestClient.post()
                .uri("/api/v1/lawyers")
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    private String deriveFirstname(String username) {
        if (username == null || username.isBlank()) return "Usuario";
        var sanitized = username.trim();
        var parts = sanitized.split("[._-]", 2);
        return capitalize(parts[0]);
    }

    private String deriveLastname(String username) {
        if (username == null || username.isBlank()) return DEFAULT_LASTNAME;
        var sanitized = username.trim();
        var parts = sanitized.split("[._-]", 2);
        return parts.length > 1 ? capitalize(parts[1]) : DEFAULT_LASTNAME;
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) return "Usuario";
        var lower = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String generatePlaceholderDni(UUID userId) {
        var hash = Math.abs(userId.getMostSignificantBits() ^ userId.getLeastSignificantBits());
        var numeric = hash % 100_000_000L;
        var digits = String.format("%08d", numeric);
        char letter = (char) ('A' + (hash % 26));
        return digits + letter;
    }

    private record ContactInfoPayload(String phoneNumber, String address) {
    }

    private record CreateClientPayload(
            UUID userId,
            String firstname,
            String lastname,
            String dni,
            ContactInfoPayload contactInfo
    ) {
    }

    private record CreateLawyerPayload(
            UUID userId,
            String firstname,
            String lastname,
            String dni,
            ContactInfoPayload contactInfo,
            String description,
            Set<String> specialties
    ) {
    }
}

