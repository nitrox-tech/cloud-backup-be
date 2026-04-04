package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        AuthUser user,
        @JsonProperty("telegram_client") TelegramClientRulesResponse telegramClient) {

    public static AuthResponse of(
            String jwt,
            String systemUserId,
            String telegramUserId,
            String username,
            TelegramClientRulesResponse telegramClient) {
        return new AuthResponse(
                jwt,
                "Bearer",
                new AuthUser(systemUserId, telegramUserId, username),
                telegramClient);
    }

    /**
     * @param id internal user id
     * @param telegramUserId Telegram account id (opaque string)
     */
    public record AuthUser(
            String id,
            @JsonProperty("telegram_user_id") String telegramUserId,
            String username) {}
}
