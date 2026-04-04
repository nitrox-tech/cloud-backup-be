package com.nitro.tech.cloud.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.api")
public class ApiKeyProperties {

    /**
     * Shared secret: client must send this value in the configured header on every request (except health
     * and CORS preflight).
     */
    @NotBlank(message = "app.api.key must be set (e.g. environment variable API_KEY)")
    private String key;

    /** HTTP header name (default matches common convention). */
    @NotBlank
    private String headerName = "X-API-Key";
}
