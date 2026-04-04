package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Trust-based login: client sends Telegram user id (and optional profile fields). No server-side hash
 * verification against a bot token.
 */
public record TelegramLoginRequest(
        @NotNull Long id,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        @JsonProperty("photo_url") String photoUrl,
        @JsonProperty("auth_date") Long authDate,
        String hash) {}
