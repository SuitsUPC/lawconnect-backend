package com.qu3dena.lawconnect.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the API Gateway.
 * 
 * This gateway handles:
 * - Request routing to microservices
 * - Load balancing
 * - Cross-cutting concerns (authentication, logging)
 * 
 * @author LawConnect Team
 * @since 2.0.0
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}

