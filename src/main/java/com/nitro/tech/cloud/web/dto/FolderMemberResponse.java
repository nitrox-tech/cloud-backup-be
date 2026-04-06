package com.nitro.tech.cloud.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nitro.tech.cloud.domain.FolderMember;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Thành viên được mời vào folder shareable")
public record FolderMemberResponse(
        @Schema(description = "UUID membership") String id,
        @JsonProperty("folder_id") @Schema(description = "UUID folder") String folderId,
        @JsonProperty("user_id") @Schema(description = "UUID user") String userId,
        @JsonProperty("created_at") @Schema(description = "UTC") Instant createdAt) {

    public static FolderMemberResponse from(FolderMember m) {
        return new FolderMemberResponse(m.getId(), m.getFolderId(), m.getUserId(), m.getCreatedAt());
    }
}
