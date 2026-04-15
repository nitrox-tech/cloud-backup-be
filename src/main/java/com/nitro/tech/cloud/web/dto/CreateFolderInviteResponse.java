package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.domain.Folder;
import com.nitro.tech.cloud.domain.FolderInviteCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Invite vừa tạo cho archive shareable")
public record CreateFolderInviteResponse(
        @JsonProperty("invite_id") @Schema(description = "UUID invite") String inviteId,
        @JsonProperty("invite_code") @Schema(description = "Mã invite") String inviteCode,
        @JsonProperty("folder_id") @Schema(description = "UUID root folder backend") String folderId,
        @JsonProperty("folder_name") @Schema(description = "Tên root folder") String folderName,
        @JsonProperty("telegram_join_link") @Schema(description = "Link join Telegram do owner cung cấp") String telegramJoinLink,
        @JsonProperty("invite_url") @Schema(description = "Deep link/web url backend build từ metadata") String inviteUrl,
        @JsonProperty("expires_at") @Schema(description = "UTC, null nếu không hết hạn") Instant expiresAt,
        @JsonProperty("created_at") @Schema(description = "UTC") Instant createdAt) {

    public static CreateFolderInviteResponse from(FolderInviteCode inviteCode, Folder rootFolder, String inviteUrl) {
        return new CreateFolderInviteResponse(
                inviteCode.getId(),
                inviteCode.getCode(),
                rootFolder.getId(),
                rootFolder.getName(),
                inviteCode.getTelegramJoinLink(),
                inviteUrl,
                inviteCode.getExpiresAt(),
                inviteCode.getCreatedAt());
    }
}
