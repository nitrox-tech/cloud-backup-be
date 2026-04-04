package com.nitro.tech.cloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * HMAC secret; for HS256 use at least 256 bits (32 ASCII chars).
     */
    private String secret = "";

    private long expirationMs = 86_400_000L;
}
