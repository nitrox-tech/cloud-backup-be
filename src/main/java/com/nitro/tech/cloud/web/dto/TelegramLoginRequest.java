package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Trust-based login: client sends Telegram user id (and optional profile fields). No server-side hash
 * verification against a bot token.
 */
@Schema(description = "Payload đăng nhập Telegram — server tin user id do client gửi")
public record TelegramLoginRequest(
        @NotNull @Schema(description = "Telegram user id (numeric)", example = "123456789") Long id,
        @JsonProperty("first_name") @Schema(description = "Tên") String firstName,
        @JsonProperty("last_name") @Schema(description = "Họ") String lastName,
        @Schema(description = "Username Telegram") String username,
        @JsonProperty("photo_url") @Schema(description = "URL ảnh (widget)") String photoUrl,
        @JsonProperty("auth_date") @Schema(description = "Unix time (widget)") Long authDate,
        @Schema(description = "Hash widget — server hiện không verify") String hash) {}
