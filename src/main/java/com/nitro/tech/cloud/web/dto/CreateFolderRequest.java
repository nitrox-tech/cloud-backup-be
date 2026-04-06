package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Tạo folder — root nếu parent_id null")
public record CreateFolderRequest(
        @NotBlank @Size(max = 256) @Schema(description = "Tên folder (unique trong cùng parent)") String name,
        @JsonProperty("parent_id") @Schema(description = "UUID folder cha; null = root") String parentId,
        @Schema(description = "Chỉ meaningful khi root: archive chia sẻ") Boolean shareable,
        @JsonProperty("telegram_chat_id") @Size(max = 200) @Schema(description = "Chat id supergroup (-100…) khi shareable root")
                String telegramChatId) {

    public CreateFolderRequest {
        if (shareable == null) {
            shareable = false;
        }
    }
}
