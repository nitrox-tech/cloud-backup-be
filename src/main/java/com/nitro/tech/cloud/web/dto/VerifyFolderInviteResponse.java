package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.service.FolderService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Kết quả verify invite code")
public record VerifyFolderInviteResponse(
        @Schema(description = "true nếu invite còn hợp lệ") boolean valid,
        @Schema(description = "Lý do invalid, null nếu valid") String reason,
        @JsonProperty("invite_id") @Schema(description = "UUID invite") String inviteId,
        @JsonProperty("invite_code") @Schema(description = "Mã invite đã normalize") String inviteCode,
        @JsonProperty("invite_url") @Schema(description = "Deep link/web url backend build từ metadata") String inviteUrl,
        @JsonProperty("telegram_join_link") @Schema(description = "Link join Telegram do owner cung cấp") String telegramJoinLink,
        @JsonProperty("expires_at") @Schema(description = "UTC, null nếu không hết hạn") Instant expiresAt,
        @JsonProperty("folder_id") @Schema(description = "UUID root folder backend") String folderId,
        @JsonProperty("folder_name") @Schema(description = "Tên root folder") String folderName,
        @JsonProperty("telegram_chat_id") @Schema(description = "Chat id Telegram của archive root") String telegramChatId) {

    public static VerifyFolderInviteResponse from(FolderService.InviteVerificationResult result) {
        if (!result.valid()) {
            return new VerifyFolderInviteResponse(false, result.reason(), null, null, null, null, null, null, null, null);
        }
        var inviteCode = result.inviteCode();
        var root = result.rootFolder();
        return new VerifyFolderInviteResponse(
                true,
                null,
                inviteCode.getId(),
                inviteCode.getCode(),
                result.inviteUrl(),
                inviteCode.getTelegramJoinLink(),
                inviteCode.getExpiresAt(),
                root.getId(),
                root.getName(),
                root.getTelegramChatId());
    }
}
