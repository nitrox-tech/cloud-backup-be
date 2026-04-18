package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/** Thông tin user gắn với folder/file (creator / owner row trong DB). */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User — dùng trong CloudEntryResponse.created_by")
public record CloudUserResponse(
        @Schema(description = "UUID user nội bộ") String id,
        @JsonProperty("telegram_user_id") @Schema(description = "Telegram user id (opaque)") String telegramUserId,
        @Schema(description = "Username Telegram nếu có") String username,
        @JsonProperty("created_at") @Schema(description = "UTC đăng ký user") Instant createdAt) {

    public static CloudUserResponse fromEntity(User u) {
        if (u == null) {
            return null;
        }
        return new CloudUserResponse(u.getId(), u.getTelegramUserId(), u.getUsername(), u.getCreatedAt());
    }
}
