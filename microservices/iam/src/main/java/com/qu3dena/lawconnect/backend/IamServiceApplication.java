package com.qu3dena.lawconnect.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing

/**
 * Main application class for the IAM (Identity and Access Management) Microservice.
 * 
 * This microservice handles:
 * - User authentication (sign-up, sign-in)
 * - JWT token generation and validation
 * - User and role management
 * - Authorization
 * 
 * @author LawConnect Team
 * @since 2.0.0
 */
@SpringBootApplication
@EntityScan(basePackages = "com.qu3dena.lawconnect.backend")
@EnableJpaRepositories(basePackages = "com.qu3dena.lawconnect.backend.iam.infrastructure.persistence.jpa.repositories")
public class IamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamServiceApplication.class, args);
    }

}

