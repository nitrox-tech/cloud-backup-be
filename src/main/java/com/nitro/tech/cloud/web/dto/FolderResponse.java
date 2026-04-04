package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.domain.Folder;
import java.time.Instant;

public record FolderResponse(
        String id,
        @JsonProperty("user_id") String userId,
        String name,
        @JsonProperty("parent_id") String parentId,
        @JsonProperty("root_folder_id") String rootFolderId,
        boolean shareable,
        @JsonProperty("telegram_chat_id") String telegramChatId,
        @JsonProperty("is_root") boolean root,
        @JsonProperty("created_at") Instant createdAt) {

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
