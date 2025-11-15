package com.qu3dena.lawconnect.backend.iam.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Configuration class that exposes a {@link RestClient} bean configured to communicate
 * with the Profiles microservice.
 */
@Configuration
public class ProfilesServiceClientConfiguration {

    /**
     * Creates a {@link RestClient} pre-configured with the Profiles service base URL.
     *
     * @param profilesServiceBaseUrl the base URL where the Profiles service is available
     * @return a {@link RestClient} ready to be injected in outbound services
     */
    @Bean
    public RestClient profilesRestClient(
            @Value("${profiles.service.url}") String profilesServiceBaseUrl
    ) {
        return RestClient.builder()
                .baseUrl(profilesServiceBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

