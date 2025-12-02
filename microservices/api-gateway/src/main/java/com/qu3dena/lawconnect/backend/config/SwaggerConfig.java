package com.qu3dena.lawconnect.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger configuration for API Gateway.
 * This configuration creates a centralized Swagger UI that aggregates all microservices.
 * 
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * All services are accessible through the API Gateway on port 8080.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LawConnect API - Centralized Documentation")
                        .description("""
                            API Gateway - Centralized documentation for all LawConnect microservices.
                            
                            All endpoints are accessible through port 8080:
                            - IAM Service: /api/v1/authentication/**, /api/v1/users/**, /api/v1/roles/**
                            - Profiles Service: /api/v1/lawyers/**, /api/v1/clients/**, /api/v1/lawyer-specialties/**
                            - Cases Service: /api/v1/cases/**, /api/v1/applications/**, /api/v1/invitations/**, /api/v1/comments/**
                            
                            Use the dropdown above to switch between different service documentations.
                            """)
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway - Main Entry Point")
                ));
    }
}

