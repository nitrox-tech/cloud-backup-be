package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.service.ArchiveInviteLink;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Link mời bạn — archive shareable + supergroup Telegram")
public record ArchiveInviteLinkResponse(
        @JsonProperty("invite_url")
                @Schema(
                        description =
                                "URL/deep link để mở app hoặc web; có folder_id và telegram_chat_id (query). Null nếu base-url rỗng.")
                String inviteUrl,
        @JsonProperty("folder_id") @Schema(description = "UUID root folder trong backend (archive shareable)") String folderId,
        @JsonProperty("telegram_chat_id")
                @Schema(description = "Telegram supergroup chat id (vd. -100…) gắn với root shareable")
                String telegramChatId) {

    public static ArchiveInviteLinkResponse from(ArchiveInviteLink link) {
        return new ArchiveInviteLinkResponse(link.inviteUrl(), link.folderId(), link.telegramChatId());
    }
}
