package com.nitro.tech.cloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.invite")
public class InviteProperties {

    /**
     * Deep link or web URL without trailing {@code ?}. Query params {@code folder_id} and {@code telegram_chat_id} are
     * appended.
     */
    private String baseUrl = "nitro-tech-cloud://invite";
}
