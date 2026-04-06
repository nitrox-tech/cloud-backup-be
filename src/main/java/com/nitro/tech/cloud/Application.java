package com.nitro.tech.cloud;

import com.nitro.tech.cloud.config.ApiKeyProperties;
import com.nitro.tech.cloud.config.InviteProperties;
import com.nitro.tech.cloud.config.JwtProperties;
import com.nitro.tech.cloud.config.TelegramClientRulesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA is enabled via {@code spring-boot-starter-data-jpa}. {@link EntityScan} / {@link EnableJpaRepositories} make
 * entity and repository packages explicit (same as default sub-package scan from this class).
 */
@SpringBootApplication
@EntityScan(basePackages = "com.nitro.tech.cloud.domain")
@EnableJpaRepositories(basePackages = "com.nitro.tech.cloud.repository")
@EnableConfigurationProperties({
    JwtProperties.class,
    TelegramClientRulesProperties.class,
    ApiKeyProperties.class,
    InviteProperties.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
