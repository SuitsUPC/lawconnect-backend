package com.qu3dena.lawconnect.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * CORS configuration for the API Gateway.
 * <p>
 * Allows requests from any origin (useful for development).
 * <p>
 * NOTE: In production, you should restrict this to specific origins for security.
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@Configuration
public class CorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        org.springframework.web.cors.CorsConfiguration corsConfig = new org.springframework.web.cors.CorsConfiguration();
        
        // Allow requests from any origin (development mode)
        // In production, use: corsConfig.addAllowedOrigin("https://yourdomain.com")
        corsConfig.addAllowedOriginPattern("*");
        
        // Allow all HTTP methods
        corsConfig.addAllowedMethod("GET");
        corsConfig.addAllowedMethod("POST");
        corsConfig.addAllowedMethod("PUT");
        corsConfig.addAllowedMethod("DELETE");
        corsConfig.addAllowedMethod("OPTIONS");
        corsConfig.addAllowedMethod("PATCH");
        
        // Allow all headers
        corsConfig.addAllowedHeader("*");
        
        // Note: setAllowCredentials(true) is not compatible with addAllowedOriginPattern("*")
        // If you need credentials, use specific origins instead of "*"
        // corsConfig.setAllowCredentials(true);
        
        // How long the response from a pre-flight request can be cached (in seconds)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}

