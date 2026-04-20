package com.stegacrypt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * StegaCryptApplication.java
 * Main entry point for StegaCrypt backend service.
 */
@SpringBootApplication
public class StegaCryptApplication {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println(" StegaCrypt Backend - Starting");
        System.out.println(" Hybrid RSA + AES Steganography Service");
        System.out.println("==============================================");

        SpringApplication.run(StegaCryptApplication.class, args);

        System.out.println("\nStegaCrypt API is running");
        System.out.println("API Endpoints:");
        System.out.println("   POST /api/generate-keys   - Generate RSA key pair");
        System.out.println("   POST /api/embed           - Hide message using public key");
        System.out.println("   POST /api/extract         - Extract message using private key");
        System.out.println("   POST /api/capacity        - Check image capacity");
        System.out.println("   POST /api/auth/login      - Login to secure chat");
        System.out.println("   POST /api/auth/register   - Register secure chat account");
        System.out.println("   GET  /api/auth/chat       - Load secure chat members and timeline");
        System.out.println("   GET  /api/health          - Health check");
        System.out.println("\nAccess at: http://localhost:8080\n");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns(allowedOrigins.split(","))
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }
}
