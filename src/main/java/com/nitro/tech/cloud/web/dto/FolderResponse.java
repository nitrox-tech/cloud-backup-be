package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.domain.Folder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Folder / archive trong DB")
public record FolderResponse(
        @Schema(description = "UUID folder") String id,
        @JsonProperty("user_id") @Schema(description = "User tạo cây archive") String userId,
        @Schema(description = "Tên") String name,
        @JsonProperty("parent_id") @Schema(description = "UUID cha; null = root") String parentId,
        @JsonProperty("root_folder_id") @Schema(description = "UUID root của cây") String rootFolderId,
        @Schema(description = "Root shareable hay không") boolean shareable,
        @JsonProperty("telegram_chat_id") @Schema(description = "Chat supergroup khi share") String telegramChatId,
        @JsonProperty("is_root") @Schema(description = "Có phải root không") boolean root,
        @JsonProperty("created_at") @Schema(description = "UTC") Instant createdAt) {

    public static FolderResponse from(Folder f) {
        return new FolderResponse(
                f.getId(),
                f.getUserId(),
                f.getName(),
                f.getParentId(),
                f.getRootFolderId(),
                f.isShareable(),
                f.getTelegramChatId(),
                f.isRoot(),
                f.getCreatedAt());
    }
}
