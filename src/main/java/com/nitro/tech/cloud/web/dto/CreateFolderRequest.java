package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFolderRequest(
        @NotBlank @Size(max = 256) String name,
        @JsonProperty("parent_id") String parentId,
        Boolean shareable,
        @JsonProperty("telegram_chat_id") @Size(max = 200) String telegramChatId) {

    public CreateFolderRequest {
        if (shareable == null) {
            shareable = false;
        }
    }
}
