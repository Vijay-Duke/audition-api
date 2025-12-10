package com.audition.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the Audition API integration.
 * Provides type-safe access to externalized configuration for API endpoints and timeouts.
 */
@Data
@ConfigurationProperties(prefix = "audition.api")
@Validated
public class AuditionApiProperties {

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5_000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 10_000;

    @NotBlank(message = "Base URL must not be blank")
    private String baseUrl;

    @NotBlank(message = "Posts path must not be blank")
    private String postsPath;

    @NotBlank(message = "Comments path must not be blank")
    private String commentsPath;

    @NotBlank(message = "Circuit breaker name must not be blank")
    private String circuitBreakerName = "auditionApi";

    @Positive(message = "Connect timeout must be positive")
    private int connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;

    @Positive(message = "Read timeout must be positive")
    private int readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
}
