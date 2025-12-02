package com.qu3dena.lawconnect.backend.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * <p>
 * Sets up the OpenAPI specification for the LawConnect Backend,
 * including API metadata, external documentation, and JWT security scheme.
 */
@Configuration
public class OpenApiConfiguration {
    /**
     * The application name, injected from properties.
     */
    @Value("${spring.application.name}")
    String applicationName;

    /**
     * The application description, injected from properties.
     */
    @Value("${documentation.application.description}")
    String applicationDescription;

    /**
     * The application version, injected from properties.
     */
    @Value("${documentation.application.version}")
    String applicationVersion;

    @Value("${documentation.server.url:https://garcia-guardian-yields-editorial.trycloudflare.com}")
    String serverUrl;

    /**
     * Configures the OpenAPI documentation for the application.
     * <p>
     * Sets API info, external documentation, and JWT bearer authentication.
     *
     * @return the configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI openAPI() {

        var openApi = new OpenAPI();

        openApi.info(new Info()
                        .title(applicationName)
                        .description(applicationDescription)
                        .version(applicationVersion)
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("LawConnect Backend Documentation")
                        .url("https://acme-learning-platform.wiki.github.io/docs"));

        // Add security definitions
        String securitySchemeName = "bearerAuth";
        openApi.addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));

        openApi
                .addServersItem(new Server()
                        .url(serverUrl)
                        .description("Cloud deployment"))
                .addServersItem(new Server()
                        .url("http://localhost:8083")
                        .description("Local Docker"));

        // Return the OpenAPI object with the configuration
        return openApi;
    }
}
