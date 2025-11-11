package com.qu3dena.lawconnect.backend.iam.infrastructure.persistence.jpa;

import com.qu3dena.lawconnect.backend.iam.domain.model.aggregates.UserAggregate;
import com.qu3dena.lawconnect.backend.iam.domain.model.entities.Role;
import com.qu3dena.lawconnect.backend.iam.domain.model.valueobjects.Roles;
import com.qu3dena.lawconnect.backend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.qu3dena.lawconnect.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Data seeder for initializing the IAM database with sample data.
 * <p>
 * This class creates roles and users with encoded passwords for testing and development purposes.
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Configuration
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner initIamDatabase(
            RoleRepository roleRepository,
            UserRepository userRepository,
            @org.springframework.beans.factory.annotation.Qualifier("passwordEncoder") PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Check if data already exists
            if (roleRepository.count() > 0) {
                logger.info("IAM database already contains data. Skipping seeding.");
                return;
            }

            logger.info("Starting IAM database seeding...");

            // Create Roles
            Role adminRole = new Role(Roles.ROLE_ADMIN);
            Role lawyerRole = new Role(Roles.ROLE_LAWYER);
            Role clientRole = new Role(Roles.ROLE_CLIENT);

            roleRepository.save(adminRole);
            roleRepository.save(lawyerRole);
            roleRepository.save(clientRole);

            logger.info("✓ Created 3 roles");

            // Create Admin User
            UserAggregate admin = UserAggregate.create(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    adminRole
            );
            userRepository.save(admin);

            // Create Lawyer Users
            UserAggregate lawyer1 = UserAggregate.create(
                    "maria.rodriguez",
                    passwordEncoder.encode("password123"),
                    lawyerRole
            );
            UserAggregate lawyer2 = UserAggregate.create(
                    "carlos.mendez",
                    passwordEncoder.encode("password123"),
                    lawyerRole
            );
            UserAggregate lawyer3 = UserAggregate.create(
                    "ana.torres",
                    passwordEncoder.encode("password123"),
                    lawyerRole
            );
            UserAggregate lawyer4 = UserAggregate.create(
                    "pedro.sanchez",
                    passwordEncoder.encode("password123"),
                    lawyerRole
            );
            UserAggregate lawyer5 = UserAggregate.create(
                    "lucia.garcia",
                    passwordEncoder.encode("password123"),
                    lawyerRole
            );

            userRepository.save(lawyer1);
            userRepository.save(lawyer2);
            userRepository.save(lawyer3);
            userRepository.save(lawyer4);
            userRepository.save(lawyer5);

            logger.info("✓ Created 5 lawyer users");

            // Create Client Users
            UserAggregate client1 = UserAggregate.create(
                    "juan.perez",
                    passwordEncoder.encode("password123"),
                    clientRole
            );
            UserAggregate client2 = UserAggregate.create(
                    "sofia.martinez",
                    passwordEncoder.encode("password123"),
                    clientRole
            );
            UserAggregate client3 = UserAggregate.create(
                    "roberto.lopez",
                    passwordEncoder.encode("password123"),
                    clientRole
            );
            UserAggregate client4 = UserAggregate.create(
                    "carmen.diaz",
                    passwordEncoder.encode("password123"),
                    clientRole
            );
            UserAggregate client5 = UserAggregate.create(
                    "miguel.fernandez",
                    passwordEncoder.encode("password123"),
                    clientRole
            );
            UserAggregate client6 = UserAggregate.create(
                    "isabel.ruiz",
                    passwordEncoder.encode("password123"),
                    clientRole
            );
            UserAggregate client7 = UserAggregate.create(
                    "diego.morales",
                    passwordEncoder.encode("password123"),
                    clientRole
            );

            userRepository.save(client1);
            userRepository.save(client2);
            userRepository.save(client3);
            userRepository.save(client4);
            userRepository.save(client5);
            userRepository.save(client6);
            userRepository.save(client7);

            logger.info("✓ Created 7 client users");

            logger.info("========================================");
            logger.info("IAM Database seeding completed!");
            logger.info("Total users created: {}", userRepository.count());
            logger.info("========================================");
            logger.info("Sample credentials:");
            logger.info("Admin:  admin / admin123");
            logger.info("Lawyer: maria.rodriguez / password123");
            logger.info("Client: juan.perez / password123");
            logger.info("========================================");
        };
    }
}

