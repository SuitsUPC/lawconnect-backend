package com.qu3dena.lawconnect.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * CORS configuration for the API Gateway.
 * <p>
 * Allows requests from the frontend application running on localhost:3000
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Configuration
public class CorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        org.springframework.web.cors.CorsConfiguration corsConfig = new org.springframework.web.cors.CorsConfiguration();
        
        // Allow requests from frontend
        corsConfig.addAllowedOrigin("http://localhost:3000");
        corsConfig.addAllowedOrigin("http://localhost:5173"); // Vite default port
        corsConfig.addAllowedOrigin("http://localhost:4200"); // Angular default port
        
        // Allow all HTTP methods
        corsConfig.addAllowedMethod("GET");
        corsConfig.addAllowedMethod("POST");
        corsConfig.addAllowedMethod("PUT");
        corsConfig.addAllowedMethod("DELETE");
        corsConfig.addAllowedMethod("OPTIONS");
        corsConfig.addAllowedMethod("PATCH");
        
        // Allow all headers
        corsConfig.addAllowedHeader("*");
        
        // Allow credentials (cookies, authorization headers, etc)
        corsConfig.setAllowCredentials(true);
        
        // How long the response from a pre-flight request can be cached (in seconds)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}

