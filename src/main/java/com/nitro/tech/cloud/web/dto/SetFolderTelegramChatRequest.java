package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Set {@code telegram_chat_id} on a shareable root. To unlink the group, use {@code DELETE /folders/{id}/telegram-chat}. */
public record SetFolderTelegramChatRequest(
        @NotBlank
        @Size(max = 200)
        @JsonProperty("telegram_chat_id")
        String telegramChatId) {}
