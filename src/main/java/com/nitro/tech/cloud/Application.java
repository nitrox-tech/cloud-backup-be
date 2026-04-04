package com.nitro.tech.cloud;

import com.nitro.tech.cloud.config.ApiKeyProperties;
import com.nitro.tech.cloud.config.JwtProperties;
import com.nitro.tech.cloud.config.TelegramClientRulesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, TelegramClientRulesProperties.class, ApiKeyProperties.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
