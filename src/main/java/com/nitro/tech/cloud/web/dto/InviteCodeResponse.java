package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.domain.FolderInviteCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Mã mời vào archive shareable")
public record InviteCodeResponse(
        @Schema(description = "UUID invite code") String id,
        @JsonProperty("invite_code") @Schema(description = "Mã mời dùng để join archive") String inviteCode,
        @JsonProperty("folder_id") @Schema(description = "UUID root folder trong backend") String folderId,
        @JsonProperty("created_at") @Schema(description = "UTC") Instant createdAt) {

    public static InviteCodeResponse from(FolderInviteCode inviteCode) {
        return new InviteCodeResponse(
                inviteCode.getId(), inviteCode.getCode(), inviteCode.getFolderId(), inviteCode.getCreatedAt());
    }
}
