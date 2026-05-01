package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response sau đăng nhập — JWT + cấu hình client Telegram")
public record AuthResponse(
        @JsonProperty("access_token") @Schema(description = "JWT dùng Authorization: Bearer") String accessToken,
        @JsonProperty("token_type") @Schema(description = "Luôn Bearer") String tokenType,
        @Schema(description = "Thông tin user") AuthUser user,
        @JsonProperty("rules") @Schema(description = "Snapshot quy tắc client") TelegramClientRulesResponse rules) {

    public static AuthResponse of(
            String jwt,
            String systemUserId,
            String telegramUserId,
            String username,
            TelegramClientRulesResponse rules) {
        return new AuthResponse(
                jwt,
                "Bearer",
                new AuthUser(systemUserId, telegramUserId, username),
                rules);
    }

    /**
     * @param id internal user id
     * @param telegramUserId Telegram account id (opaque string)
     */
    @Schema(description = "User sau login")
    public record AuthUser(
            @Schema(description = "UUID user nội bộ") String id,
            @JsonProperty("telegram_user_id") @Schema(description = "Id tài khoản Telegram") String telegramUserId,
            @Schema(description = "Username Telegram") String username) {}
}
