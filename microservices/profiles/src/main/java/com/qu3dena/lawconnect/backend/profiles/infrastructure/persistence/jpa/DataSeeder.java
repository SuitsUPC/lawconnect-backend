package com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa;

import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.ClientAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.aggregates.LawyerAggregate;
import com.qu3dena.lawconnect.backend.profiles.domain.model.entities.LawyerSpecialty;
import com.qu3dena.lawconnect.backend.profiles.domain.model.valueobjects.*;
import com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa.repositories.ClientRepository;
import com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa.repositories.LawyerRepository;
import com.qu3dena.lawconnect.backend.profiles.infrastructure.persistence.jpa.repositories.LawyerSpecialtyRepository;
import com.qu3dena.lawconnect.backend.shared.domain.model.valueobjects.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.*;

/**
 * Data seeder for initializing the Profiles database with sample data.
 * <p>
 * Queries IAM service to get real user IDs and creates profiles with those IDs.
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Configuration
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner initProfilesDatabase(
            LawyerSpecialtyRepository specialtyRepository,
            LawyerRepository lawyerRepository,
            ClientRepository clientRepository
    ) {
        return args -> {
            // Check if data already exists
            if (specialtyRepository.count() > 0) {
                logger.info("Profiles database already contains data. Skipping seeding.");
                return;
            }

            logger.info("Starting Profiles database seeding...");

            // Wait for IAM service to be ready
            logger.info("Waiting for IAM service to initialize...");
            Thread.sleep(10000);

            // Get users from IAM service
            RestTemplate restTemplate = new RestTemplate();
            String iamServiceUrl = "http://iam-service:8081/api/v1/users";
            
            List<Map<String, Object>> users = null;
            int retries = 5;
            while (retries > 0 && users == null) {
                try {
                    var response = restTemplate.exchange(
                            iamServiceUrl,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                    );
                    users = response.getBody();
                    logger.info("Successfully retrieved {} users from IAM service", users.size());
                } catch (Exception e) {
                    logger.warn("Failed to connect to IAM service, retrying... ({} attempts left)", retries);
                    Thread.sleep(5000);
                    retries--;
                }
            }

            if (users == null || users.isEmpty()) {
                logger.error("Could not retrieve users from IAM service. Skipping profile seeding.");
                return;
            }

            // Extract user IDs by username (to ensure correct mapping)
            Map<String, UUID> userIdsByUsername = new HashMap<>();
            
            for (Map<String, Object> user : users) {
                String username = (String) user.get("username");
                String idStr = (String) user.get("id");
                UUID userId = UUID.fromString(idStr);
                userIdsByUsername.put(username, userId);
            }

            logger.info("Found {} users from IAM service", userIdsByUsername.size());

            // Create Lawyer Specialties
            LawyerSpecialty criminalLaw = specialtyRepository.save(new LawyerSpecialty(LawyerSpecialties.CRIMINAL_LAW));
            LawyerSpecialty civilLitigation = specialtyRepository.save(new LawyerSpecialty(LawyerSpecialties.CIVIL_LITIGATION));
            LawyerSpecialty familyLaw = specialtyRepository.save(new LawyerSpecialty(LawyerSpecialties.FAMILY_LAW));
            LawyerSpecialty corporateLaw = specialtyRepository.save(new LawyerSpecialty(LawyerSpecialties.CORPORATE_LAW));
            LawyerSpecialty taxLaw = specialtyRepository.save(new LawyerSpecialty(LawyerSpecialties.TAX_LAW));
            LawyerSpecialty intellectualProperty = specialtyRepository.save(new LawyerSpecialty(LawyerSpecialties.INTELLECTUAL_PROPERTY));
            LawyerSpecialty realEstateLaw = specialtyRepository.save(new LawyerSpecialty(LawyerSpecialties.REAL_ESTATE_LAW));
            LawyerSpecialty employmentLaw = specialtyRepository.save(new LawyerSpecialty(LawyerSpecialties.EMPLOYMENT_LAW));
            LawyerSpecialty personalInjury = specialtyRepository.save(new LawyerSpecialty(LawyerSpecialties.PERSONAL_INJURY));
            LawyerSpecialty estatePlanning = specialtyRepository.save(new LawyerSpecialty(LawyerSpecialties.ESTATE_PLANNING));

            logger.info("✓ Created {} lawyer specialties", specialtyRepository.count());

            // Create Lawyer Profiles using real user IDs matched by username
            UUID mariaId = userIdsByUsername.get("maria.rodriguez");
            UUID carlosId = userIdsByUsername.get("carlos.mendez");
            UUID anaId = userIdsByUsername.get("ana.torres");
            UUID pedroId = userIdsByUsername.get("pedro.sanchez");
            UUID luciaId = userIdsByUsername.get("lucia.garcia");

            if (mariaId != null && carlosId != null && anaId != null && pedroId != null && luciaId != null) {
                LawyerAggregate lawyer1 = LawyerAggregate.create(
                        mariaId,
                        new FullName("María", "Rodríguez"),
                        new ContactInfo("+51 987654321", "Av. Javier Prado 123, San Isidro, Lima"),
                        new Dni("12345678A"),
                        new Description("Abogada especializada en derecho penal con 15 años de experiencia."),
                        null
                );
                lawyerRepository.save(lawyer1);

                LawyerAggregate lawyer2 = LawyerAggregate.create(
                        carlosId,
                        new FullName("Carlos", "Méndez"),
                        new ContactInfo("+51 998765432", "Calle Las Begonias 456, San Borja, Lima"),
                        new Dni("23456789B"),
                        new Description("Especialista en derecho corporativo y fusiones empresariales."),
                        null
                );
                lawyerRepository.save(lawyer2);

                LawyerAggregate lawyer3 = LawyerAggregate.create(
                        anaId,
                        new FullName("Ana", "Torres"),
                        new ContactInfo("+51 987123456", "Jr. Camaná 789, Lima Centro, Lima"),
                        new Dni("34567890C"),
                        new Description("Abogada de familia con enfoque en mediación y resolución pacífica de conflictos."),
                        null
                );
                lawyerRepository.save(lawyer3);

                LawyerAggregate lawyer4 = LawyerAggregate.create(
                        pedroId,
                        new FullName("Pedro", "Sánchez"),
                        new ContactInfo("+51 991234567", "Av. Arequipa 1010, Miraflores, Lima"),
                        new Dni("45678901D"),
                        new Description("Experto en derecho inmobiliario y contratos de propiedad."),
                        null
                );
                lawyerRepository.save(lawyer4);

                LawyerAggregate lawyer5 = LawyerAggregate.create(
                        luciaId,
                        new FullName("Lucía", "García"),
                        new ContactInfo("+51 992345678", "Calle Schell 222, Miraflores, Lima"),
                        new Dni("56789012E"),
                        new Description("Abogada laboralista especializada en defensa de derechos de trabajadores."),
                        null
                );
                lawyerRepository.save(lawyer5);

                logger.info("✓ Created {} lawyer profiles", lawyerRepository.count());
            } else {
                logger.warn("Could not find all required lawyer users by username. Skipping lawyer profile seeding.");
            }

            // Create Client Profiles using real user IDs matched by username
            UUID juanId = userIdsByUsername.get("juan.perez");
            UUID sofiaId = userIdsByUsername.get("sofia.martinez");
            UUID robertoId = userIdsByUsername.get("roberto.lopez");
            UUID carmenId = userIdsByUsername.get("carmen.diaz");
            UUID miguelId = userIdsByUsername.get("miguel.fernandez");

            if (juanId != null && sofiaId != null && robertoId != null && carmenId != null && miguelId != null) {
                ClientAggregate client1 = ClientAggregate.create(
                        juanId,
                        new FullName("Juan", "Pérez"),
                        new ContactInfo("+51 912345678", "Av. La Marina 500, Pueblo Libre, Lima"),
                        new Dni("67890123F")
                );
                clientRepository.save(client1);

                ClientAggregate client2 = ClientAggregate.create(
                        sofiaId,
                        new FullName("Sofía", "Martínez"),
                        new ContactInfo("+51 923456789", "Calle Los Pinos 678, Surco, Lima"),
                        new Dni("78901234G")
                );
                clientRepository.save(client2);

                ClientAggregate client3 = ClientAggregate.create(
                        robertoId,
                        new FullName("Roberto", "López"),
                        new ContactInfo("+51 934567890", "Jr. Tacna 890, Lima Centro, Lima"),
                        new Dni("89012345H")
                );
                clientRepository.save(client3);

                ClientAggregate client4 = ClientAggregate.create(
                        carmenId,
                        new FullName("Carmen", "Díaz"),
                        new ContactInfo("+51 945678901", "Av. Universitaria 1234, Los Olivos, Lima"),
                        new Dni("90123456I")
                );
                clientRepository.save(client4);

                ClientAggregate client5 = ClientAggregate.create(
                        miguelId,
                        new FullName("Miguel", "Fernández"),
                        new ContactInfo("+51 956789012", "Calle Las Flores 345, Jesús María, Lima"),
                        new Dni("01234567J")
                );
                clientRepository.save(client5);

                logger.info("✓ Created {} client profiles", clientRepository.count());
            } else {
                logger.warn("Could not find all required client users by username. Skipping client profile seeding.");
            }

            logger.info("========================================");
            logger.info("Profiles Database seeding completed!");
            logger.info("Total lawyers: {}", lawyerRepository.count());
            logger.info("Total clients: {}", clientRepository.count());
            logger.info("Total specialties: {}", specialtyRepository.count());
            logger.info("========================================");
        };
    }
}
