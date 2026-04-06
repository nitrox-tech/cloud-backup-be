package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Set {@code telegram_chat_id} on a shareable root. To unlink the group, use {@code DELETE /folders/{id}/telegram-chat}. */
@Schema(description = "Gắn chat id nhóm Telegram cho root shareable")
public record SetFolderTelegramChatRequest(
        @NotBlank
        @Size(max = 200)
        @JsonProperty("telegram_chat_id")
        @Schema(description = "Id chat (vd. -100…)", example = "-1001234567890")
        String telegramChatId) {}
